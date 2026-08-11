import 'package:flutter/foundation.dart';

/// Guarda la URL base del API Gateway. Es un ValueNotifier (no persistido en
/// disco a propósito, para no agregar dependencias de plugins nativos como
/// shared_preferences a una app pensada para demo) — se configura la primera
/// vez que se abre la app, desde la pantalla de Settings.
///
/// IMPORTANTE — por qué no alcanza con "localhost" acá:
/// - Emulador Android: "localhost" del emulador NO es tu máquina. Usá
///   10.0.2.2 (alias especial del emulador hacia el host).
/// - Simulador iOS: "localhost" SÍ funciona (el simulador comparte red con
///   el host).
/// - Celular físico (USB o WiFi): necesitás la IP LAN real de tu máquina
///   (ej. 192.168.1.23), con el celular en la MISMA red WiFi que el backend.
///   Backend y celular en redes distintas (ej. datos móviles) NO van a
///   poder conectarse sin exponer el Gateway públicamente.
class ApiConfig {
  ApiConfig._();

  static final ValueNotifier<String> baseUrl = ValueNotifier<String>(
    defaultTargetPlatform == TargetPlatform.android
        ? 'http://10.0.2.2:8000' // default razonable para emulador Android
        : 'http://localhost:8000', // default razonable para simulador iOS / web
  );

  static void update(String newBaseUrl) {
    // Normaliza: sin barra final, para que la concatenación de paths sea consistente.
    final trimmed = newBaseUrl.trim();
    baseUrl.value = trimmed.endsWith('/')
        ? trimmed.substring(0, trimmed.length - 1)
        : trimmed;
  }
}
