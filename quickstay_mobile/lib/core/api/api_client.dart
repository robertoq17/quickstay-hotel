import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/api_config.dart';

/// Excepción con el mensaje que ya viene armado del backend (tanto
/// GlobalExceptionHandler como el FallbackController del Gateway devuelven
/// {timestamp, status, error, message}) — este cliente lo aprovecha en vez
/// de mostrar errores HTTP genéricos.
class ApiException implements Exception {
  final int statusCode;
  final String message;
  ApiException(this.statusCode, this.message);

  @override
  String toString() => message;
}

class ApiClient {
  static const _timeout = Duration(seconds: 8);

  static Uri _uri(String path, [Map<String, String>? query]) {
    final base = ApiConfig.baseUrl.value;
    return Uri.parse('$base$path').replace(queryParameters: query);
  }

  static Future<dynamic> get(String path, {Map<String, String>? query}) async {
    final uri = _uri(path, query);
    final response = await http.get(uri).timeout(_timeout);
    return _handle(response);
  }

  /// [acceptedStatusCodes] permite tratar como "éxito" (parsear y devolver
  /// el body) códigos fuera del rango 2xx habitual. Caso concreto: el Saga
  /// (POST /api/sagas/bookings) devuelve 409 cuando el pago se compensa —
  /// es un resultado de NEGOCIO válido (con body completo: sagaId, estado,
  /// mensaje, etc.), no un error técnico. Si tratáramos ese 409 como error
  /// genérico, perderíamos el detalle de la compensación en la UI.
  static Future<dynamic> post(
    String path,
    Map<String, dynamic> body, {
    Set<int>? acceptedStatusCodes,
  }) async {
    final uri = _uri(path);
    final response = await http
        .post(
          uri,
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(body),
        )
        .timeout(_timeout);
    return _handle(response, acceptedStatusCodes: acceptedStatusCodes);
  }

  static dynamic _handle(http.Response response, {Set<int>? acceptedStatusCodes}) {
    final status = response.statusCode;
    final isSuccess = acceptedStatusCodes != null
        ? acceptedStatusCodes.contains(status)
        : (status >= 200 && status < 300);

    dynamic decoded;
    try {
      decoded = response.body.isNotEmpty ? jsonDecode(response.body) : null;
    } catch (_) {
      decoded = null;
    }

    if (isSuccess) {
      return decoded;
    }

    // Tanto GlobalExceptionHandler (backend) como FallbackController
    // (Gateway, cuando el Circuit Breaker de Payment está abierto)
    // devuelven un campo "message" legible — lo mostramos directo.
    final message = (decoded is Map && decoded['message'] != null)
        ? decoded['message'] as String
        : 'Error del servidor ($status)';

    throw ApiException(status, message);
  }
}
