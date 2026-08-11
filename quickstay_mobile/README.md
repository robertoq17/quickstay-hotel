## Correr la app

Con todo el sistema arriba (`docker compose up -d` desde `infra/`, o cada
módulo con `./gradlew bootRun`):

```bash
flutter run
```

Elegí el emulador/dispositivo cuando te lo pida. Al abrir la app por
primera vez, ir a **Configuración** y confirmá/ajustá la URL del
Gateway antes de buscar.

---

## Guion sugerido para la demo

1. **Búsqueda**: ciudad "La Paz" o "Santa Cruz de la Sierra", fechas
   futuras, precio máximo 1000 → mostrar resultados viniendo del Gateway
   → backend → Inventory (con la proyección CQRS de Sesión VIII).
2. **Reserva feliz**: tocar "Reservar" en una habitación, completar
   nombre/email, dejar el switch de "Simular fallo" apagado → Confirmar y
   pagar → pantalla verde: Saga `COMPLETED`, reserva `CONFIRMED`, pago
   `AUTHORIZED`.
3. **Reserva con compensación**: repetir el flujo, esta vez con el switch
   de "Simular fallo de pago" activado → pantalla roja: Saga
   `COMPENSATED`, reserva `CANCELLED` — mostrar que la habitación se
   liberó automáticamente (patrón Saga, Sesión V).
4. **Consulta de estado**: copiar el Saga ID de cualquiera de los dos
   resultados, ir al ícono en el home, pegarlo y consultar — confirma
   que `GET /api/sagas/bookings/{id}` funciona igual desde un cliente
   completamente distinto al frontend web.
5. **(Opcional) Circuit Breaker**: parar `payment-service` a mitad de la
   demo y repetir el paso 2 — el Gateway debería responder con el mensaje
   de `FallbackController` en vez de colgar la app.

---

## Estructura

```
lib/
├── main.dart
├── core/
│   ├── config/api_config.dart       URL del Gateway (editable en runtime)
│   ├── api/
│   │   ├── api_client.dart          wrapper HTTP + manejo de errores
│   │   └── quickstay_repository.dart llamadas a /api/rooms, /api/sagas
│   └── models/                      espejo de los DTOs Java del backend
└── features/
    ├── search/       búsqueda de habitaciones
    ├── booking/       formulario de reserva+pago y pantalla de resultado
    ├── saga_status/   consulta de estado de un Saga por ID
    └── settings/      configuración de la URL del Gateway
```