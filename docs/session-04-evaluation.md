# Sesión IV — Enterprise Integration & Messaging: Evaluación

## De síncrono/acoplado a asíncrono/desacoplado

**Antes (Sesión III):** si quisiéramos notificar al huésped al confirmar una
reserva, la forma "obvia" en el modular monolith hubiera sido que
`ReservationService.reserve()` llamara directamente a un
`NotificationService.sendConfirmation(...)` — una llamada de método más dentro
de la misma transacción. Eso acopla Booking a Notification de forma síncrona:
si el envío del email fuera lento o fallara, la reserva completa fallaría o se
demoraría, aunque la reserva en sí ya esté perfectamente guardada en la DB.

**Ahora (Sesión IV):** `ReservationService` no conoce a Notification en
absoluto. Publica un domain event en memoria (`ApplicationEventPublisher`),
y un componente de infraestructura separado (`ReservationEventPublisher`)
lo traduce a un mensaje RabbitMQ **después de que la transacción confirma**
(`AFTER_COMMIT`). El cliente HTTP recibe su `201 Created` sin esperar a que
la notificación se procese.

## Consistencia eventual

Esto introduce **consistencia eventual** entre Booking y Notification: hay
una ventana de tiempo (normalmente milisegundos, pero no garantizado) entre
"la reserva está confirmada en la base de datos" y "el huésped recibió su
email de confirmación". Este trade-off es aceptable para notificaciones —
nadie espera un email instantáneo — pero **no** sería aceptable, por
ejemplo, para el cobro de un pago (ahí la sesión V introduce el patrón Saga
para manejar consistencia entre pasos que si necesitan coordinación más
estricta).

## Por qué `AFTER_COMMIT` y no publicar directo a RabbitMQ dentro del `@Transactional`

Si `ReservationService` llamara a `rabbitTemplate.convertAndSend(...)`
directamente dentro del método `@Transactional`, y luego la transacción de
base de datos fallara por cualquier motivo (constraint violation, timeout),
ya seríamos incapaces de "deshacer" el mensaje — ya salió a RabbitMQ y
probablemente un consumidor ya lo procesó. Terminaríamos notificando al
huésped de una reserva que en realidad nunca se guardó.

Usar el bus en memoria de Spring (`ApplicationEventPublisher` +
`@TransactionalEventListener(phase = AFTER_COMMIT)`) resuelve esto sin
necesitar un patrón más pesado como Transactional Outbox (que se justificaría
recién si tuviéramos que garantizar entrega exactamente-una-vez en un
escenario de alta criticidad — no es el caso de una notificación).

## Patrones de Enterprise Integration aplicados (Hohpe & Woolf)

- **Publish-Subscribe Channel**: el exchange `quickstay.reservation-events`
  es un `TopicExchange` — más de un consumidor podría suscribirse sin que
  Booking se entere ni tenga que cambiar código (ej. cuando se agregue
  Loyalty, va a bindear su propia cola al mismo exchange).
- **Message Translator**: `ReservationIntegrationEvent` traduce el domain
  event interno (`ReservationConfirmedEvent`) a un contrato de mensaje
  estable, y el consumidor tiene su propia traducción inversa
  (`ReservationEventMessage`).
- **Canonical Data Model** (parcial): al no compartir clases Java entre
  Bounded Contexts, el "contrato" real es el JSON — es el germen de un
  modelo de datos canónico entre dominios, aunque todavía no está formalizado
  con un schema registry (eso excede el alcance de este módulo).

## Qué NO se resolvió en esta sesión (a propósito)

El acoplamiento síncrono documentado en la Sesión III
(`ReservationService` → `RoomRepository` para validar que la habitación
existe) **sigue existiendo**. No se convirtió a mensajería porque es una
validación que necesita respuesta inmediata (no tiene sentido aceptar una
reserva "optimistamente" y enterarse async de que la habitación no existe).
Ese es precisamente el tipo de decisión que esta sesión buscaba enseñar:
**no todo se convierte a eventos** — solo lo que tolera consistencia
eventual.

## Cómo probarlo

1. Los 3 contenedores arriba: `docker compose up -d` (Postgres, pgAdmin,
   RabbitMQ).
2. Backend arriba: `./gradlew bootRun`.
3. Hacé una reserva vía `POST /api/reservations` (ver README).
4. Mirá el log del backend — debería aparecer:
   ```
   Publishing RESERVATION_CONFIRMED to exchange=quickstay.reservation-events routingKey=reservation.confirmed
   [NOTIFICATION] Enviando email de confirmación a ...
   ```
   Ambas líneas del mismo proceso, pero ocurren en threads/momentos distintos
   — la segunda depende de RabbitMQ efectivamente entregar el mensaje.
5. Abrí `http://localhost:15672` (user/pass: `quickstay`/`quickstay`) para
   ver el exchange `quickstay.reservation-events` y la cola
   `notification.reservation-events` en la UI de administración de
   RabbitMQ — útil para confirmar visualmente el flujo de mensajes.
