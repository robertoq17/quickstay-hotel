# Sesión IV — Enterprise Integration & Messaging

## Actividad 1: Map Out Domain Events

Catálogo completo de eventos de dominio identificados para QuickStay, a
partir de los 13 requerimientos de la kata. Se marca cuál está **implementado**
en esta sesión y cuál queda como **roadmap** (se implementa junto con el
Bounded Context correspondiente, en sesiones futuras).

| Evento | Origen (Bounded Context) | Consumidores | Estado |
|---|---|---|---|
| `ReservationConfirmed` | Booking | Notification, Inventory (futuro: actualizar proyección de disponibilidad) | ✅ Implementado |
| `ReservationCancelled` | Booking | Notification, Inventory (futuro) | ✅ Implementado |
| `ReservationModified` | Booking | Notification | ⏳ Roadmap (Sesión V — Saga) |
| `PaymentProcessed` | Payment | Booking (confirma reserva), Notification | ⏳ Roadmap (Sesión IV/V) |
| `PaymentFailed` | Payment | Booking (libera habitación), Notification | ⏳ Roadmap (Sesión V — Saga, compensación) |
| `LoyaltyPointsAwarded` | Loyalty | Notification | ⏳ Roadmap (Sesión VI/VIII) |
| `PromotionApplied` | Promotions | Booking, Notification | ⏳ Roadmap (Sesión VI/VIII) |
| `CheckInCompleted` | CheckIn | Notification, Housekeeping | ⏳ Roadmap (Sesión VII) |
| `CheckOutCompleted` | CheckIn | Notification, Billing | ⏳ Roadmap (Sesión VII) |
| `RoomServiceRequested` | Guest Services | Housekeeping/Maintenance | ⏳ Roadmap (fuera de alcance del módulo) |

## Por qué empezar por `ReservationConfirmed` / `ReservationCancelled`

Son los únicos eventos cuyo Bounded Context de origen (**Booking**) ya existe
con código real desde la Sesión III. Además, tienen el consumidor más
representativo del requerimiento *"Notify customers about booking
confirmation, reminders, and changes"* — así que implementarlos primero deja
el patrón de mensajería listo y probado para cuando se agreguen Payment,
Loyalty, etc. en sesiones posteriores.

## Contrato de evento vs. contrato de integración

Se distinguen deliberadamente dos conceptos, siguiendo Enterprise Integration
Patterns (Hohpe & Woolf):

- **Domain Event** (`booking.event.*`): objeto interno del dominio Booking,
  usado solo dentro del proceso Java vía `ApplicationEventPublisher` de
  Spring. Puede tener cualquier forma que le sirva a Booking.
- **Integration Event** (`booking.messaging.ReservationIntegrationEvent`):
  el mensaje que efectivamente viaja por RabbitMQ. Es un contrato más
  estable y deliberadamente más simple/plano — si el modelo interno de
  `Reservation` cambia, el contrato de integración no tiene por qué cambiar
  al mismo ritmo.

El consumidor (`notification.messaging.ReservationEventMessage`) tiene su
**propia copia** del contrato, no importa clases de `booking`. Esto es
intencional: los Bounded Contexts se comunican solo a través del mensaje
serializado (JSON), nunca compartiendo tipos Java — así, si Notification se
extrae a un microservicio en la Sesión VI, este código no cambia.
