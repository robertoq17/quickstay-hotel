# Session VI — Microservices Deep Dive: Payment Extraction

## Objetivo

Extraer físicamente un bounded context del Modular Monolith y convertirlo en una
unidad de despliegue independiente con una base de datos dedicada y aislada.

## Dominio seleccionado

Se seleccionó **Payment** porque ya estaba delimitado durante Session V y forma
parte del Saga Booking → Payment → Confirmation. Esto permite demostrar una
extracción incremental sin rehacer toda la plataforma.

## Arquitectura resultante

- `backend/`: mantiene Inventory, Booking, Saga y Notification.
- `payment-service/`: nuevo proceso Spring Boot independiente.
- PostgreSQL `quickstay` en `5433`: datos del monolito.
- PostgreSQL `quickstay_payment` en `5434`: datos exclusivos de Payment.
- `PaymentServiceClient`: cliente REST dentro del Saga Orchestrator.

## Data Isolation

El Payment Service ya no comparte entidades, repositorios ni tablas con Booking.
La relación se realiza mediante `reservationId` como identificador de negocio.
No existe FK entre las dos bases.

## Contrato

- `POST /api/payments/authorize`
- `POST /api/payments/{reservationId}/refund`
- `GET /api/payments/health`

## Compensación

Si el Payment Service falla durante la autorización, el Saga conserva el
control y cancela la reserva pendiente. Si Payment ya fue autorizado y una
etapa posterior falla, el Saga solicita `refund` al Payment Service y luego
cancela la reserva.

## Validación manual

1. Ejecutar `docker compose up -d` desde `infra/`.
2. Arrancar `backend` en `8080`.
3. Arrancar `payment-service` en `8081`.
4. Ejecutar el flujo de Booking + Payment.
5. Comprobar que `saga_executions` vive en QuickStay DB y `payments` vive en Payment DB.
6. Detener Payment Service y repetir el flujo para observar el fallo parcial y la compensación.
