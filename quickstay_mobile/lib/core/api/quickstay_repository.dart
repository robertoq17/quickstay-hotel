import '../api/api_client.dart';
import '../models/room_availability.dart';
import '../models/saga_booking_request.dart';
import '../models/saga_booking_response.dart';

class QuickstayRepository {
  /// GET /api/rooms/search — enrutado por el Gateway hacia quickstay-backend.
  static Future<List<RoomAvailability>> searchRooms({
    required String city,
    required DateTime checkIn,
    required DateTime checkOut,
    required double maxPrice,
  }) async {
    String fmt(DateTime d) =>
        '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

    final data = await ApiClient.get('/api/rooms/search', query: {
      'city': city,
      'checkIn': fmt(checkIn),
      'checkOut': fmt(checkOut),
      'maxPrice': maxPrice.toString(),
    });

    return (data as List)
        .map((e) => RoomAvailability.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  /// POST /api/sagas/bookings — enrutado por el Gateway hacia quickstay-backend,
  /// que orquesta Booking + Payment (llamando a quickstay-payment-service
  /// directo, sin pasar de nuevo por el Gateway — ver docs/session-07-evaluation.md).
  ///
  /// Acepta 201 (Saga COMPLETED) y 409 (Saga COMPENSATED/FAILED) como
  /// resultados válidos — ambos traen un SagaBookingResponse completo que
  /// la UI necesita mostrar. Solo un error real (5xx, 503 del Circuit
  /// Breaker, timeout de red) debería terminar en ApiException.
  static Future<SagaBookingResponse> bookAndPay(SagaBookingRequest request) async {
    final data = await ApiClient.post(
      '/api/sagas/bookings',
      request.toJson(),
      acceptedStatusCodes: {201, 409},
    );
    return SagaBookingResponse.fromJson(data as Map<String, dynamic>);
  }

  /// GET /api/sagas/bookings/{sagaId} — consulta el estado final de un Saga.
  static Future<SagaExecutionInfo> getSagaStatus(String sagaId) async {
    final data = await ApiClient.get('/api/sagas/bookings/$sagaId');
    return SagaExecutionInfo.fromJson(data as Map<String, dynamic>);
  }
}
