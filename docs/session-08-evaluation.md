# Sesión VIII — Cloud-Native Operations & Selection

## Actividad: Split Read & Write Models (CQRS)

**Competencia:** ejecutar la separación progresiva de las operaciones de lectura y escritura, equilibrando los beneficios de escalabilidad con la complejidad operativa.

## Objetivo de la implementación

La búsqueda de habitaciones es, por naturaleza, una operación de **alta frecuencia y predominantemente de lectura**. En cambio, reservar, confirmar y cancelar son operaciones de escritura que deben conservar las reglas de negocio y la consistencia transaccional del Bounded Context `Booking`.

La Sesión VIII introduce CQRS sin convertir todavía Inventory y Booking en dos microservicios. La separación de persistencia es explícita: PostgreSQL usa dos schemas lógicos independientes dentro de la misma instancia, `quickstay_write` y `quickstay_read`:

- **Write model (`quickstay_write`):** entidades JPA `Room`, `Hotel`, `Reservation`, `Guest` y `SagaExecution`. Es la autoridad transaccional.
- **Read model (`quickstay_read`):** tablas denormalizadas `room_availability_read_model` y `reservation_availability_read_model`, consultadas con `JdbcTemplate` exclusivamente por búsqueda.
- **Projection:** un consumidor RabbitMQ de Inventory actualiza el read model a partir de los eventos de Booking.
- **Bootstrap:** Flyway V5 crea `quickstay_read` y materializa inicialmente la proyección desde `quickstay_write`.

La decisión es incremental: se obtiene una separación CQRS verificable en PostgreSQL y una ruta clara hacia escalado independiente del READ side sin introducir todavía un segundo servidor PostgreSQL ni nuevos deployables.

---

## Antes y después

### Antes — lectura acoplada al modelo de escritura

```text
GET /api/rooms/search
        │
        ▼
RoomSearchService
        │
        ▼
RoomRepository (JPA)
        │
        ├── rooms / hotels
        └── Reservation (Booking)
```

La query de disponibilidad necesitaba conocer la tabla `reservations` para excluir habitaciones ocupadas. Eso dejaba un acoplamiento cross-domain dentro de Inventory.

### Después — CQRS

```text
                         ┌──────────────────────────┐
                         │      WRITE MODEL         │
                         │ JPA: Room / Reservation  │
                         └────────────┬─────────────┘
                                      │
                              Domain Events
                                      │
                                      ▼
                               RabbitMQ Exchange
                                      │
                                      ▼
                         ┌──────────────────────────┐
                         │ Inventory Projection     │
                         │ @RabbitListener           │
                         └────────────┬─────────────┘
                                      │
                                      ▼
                         ┌──────────────────────────┐
                         │       READ MODEL         │
                         │ denormalized SQL tables  │
                         └────────────┬─────────────┘
                                      │
GET /api/rooms/search ────────────────┘
                                      │
                                      ▼
                           JdbcTemplate / Read Repo
```

El flujo de escritura no depende del read model para confirmar una reserva, y el flujo de consulta no accede a repositorios JPA del write model. Esto preserva la autoridad transaccional del write model y hace explícita la **consistencia eventual** de la búsqueda.

---

## Modelos introducidos

### `room_availability_read_model`

Proyección denormalizada de los datos que la búsqueda necesita con frecuencia:

- `room_id`
- `hotel_id`
- `hotel_name`
- `city`
- `room_type`
- `price_per_night`
- `capacity`
- `active`
- `updated_at`

### `reservation_availability_read_model`

Proyección mínima para responder si una habitación está ocupada en un intervalo:

- `reservation_id`
- `room_id`
- `check_in`
- `check_out`
- `status`
- `updated_at`

La búsqueda considera no disponibles las reservas `PENDING_PAYMENT` y `CONFIRMED`. Esto es importante porque el Saga mantiene la habitación bloqueada mientras procesa el pago.

---

## Integración con RabbitMQ

Inventory declara y posee su propia cola:

```text
inventory.room-availability-projection
```

La cola consume del exchange:

```text
quickstay.reservation-events
```

y recibe los eventos:

```text
reservation.pending-payment
reservation.confirmed
reservation.cancelled
```

### Por qué se agregó `RESERVATION_PENDING_PAYMENT`

En Sesión V, una reserva temporal durante el Saga ya bloqueaba la habitación en el write model. Para que el read model represente exactamente la misma regla de disponibilidad, Inventory también necesita conocer ese estado.

Por eso `ReservationService.reservePendingPayment()` publica un `ReservationPendingPaymentEvent`. Notification **no** se suscribe a ese routing key, porque todavía no corresponde enviar una confirmación al huésped.

### Idempotencia de la proyección

La tabla `reservation_availability_read_model` usa `reservation_id` como PK y la proyección utiliza `INSERT ... ON CONFLICT DO UPDATE`. Si RabbitMQ entrega un evento más de una vez, el estado final de la proyección sigue siendo correcto para ese evento.

---

## Consistencia eventual y UX

CQRS introduce una propiedad que debe quedar explícita:

> Una escritura confirmada puede tardar unos instantes en reflejarse en el modelo de lectura.

Por ejemplo:

```text
Reserva confirmada
      │
      ├── Write DB: inmediatamente consistente
      │
      └── RabbitMQ → Projection → Read Model
                               ↑
                         milisegundos
```

Esto es aceptable para una plataforma donde la búsqueda soporta una carga de lectura muy superior a la de escritura, siempre que las operaciones críticas de reserva mantengan la validación transaccional en el write model.

**Importante:** CQRS no reemplaza la protección anti-overbooking. `ReservationService` continúa verificando solapamientos contra el modelo de escritura antes de crear la reserva.

---

## Decisión arquitectónica

| Operación | Modelo elegido | Razón |
|---|---|---|
| Buscar disponibilidad | Read model | Consulta optimizada y denormalizada |
| Crear reserva | Write model | Transacción + reglas de negocio |
| Confirmar reserva | Write model | Estado de Booking como autoridad |
| Cancelar reserva | Write model | Cambio de estado transaccional |
| Actualizar proyección | Asíncrona | RabbitMQ consumer | Desacoplar lectura de escritura |

### Selección

CQRS es apropiado aquí porque **no todas las operaciones tienen el mismo perfil**. Replicar o separar todo el sistema sería complejidad innecesaria; separar primero la lectura de disponibilidad ataca directamente el cuello de botella más probable.

---

## Escalabilidad y resiliencia

La separación deja una ruta natural para una evolución cloud-native:

```text
                    ┌── Read replica / read DB (futuro)
                    │
Usuarios ── Gateway ─┼── Read model × N
                    │
                    └── Write model × N
```

En esta sesión no se agrega un segundo servidor PostgreSQL ni autoscaling real. La separación CQRS ya es explícita mediante `quickstay_write` y `quickstay_read`; la separación física queda como evolución operativa si la carga lo justifica.

### Qué ganamos

- Lecturas optimizadas para el caso de uso real.
- Menor acoplamiento entre Inventory y la entidad `Reservation`.
- Posibilidad futura de escalar consumidores/proyecciones y capacidad de lectura de forma independiente.
- Proyección materializada que puede evolucionar hacia una tecnología especializada sin modificar el dominio de escritura.

### Qué pagamos

- Consistencia eventual.
- RabbitMQ pasa a formar parte del camino de actualización del read model.
- Hay dos modelos que mantener.
- Una proyección defectuosa requiere observabilidad y mecanismos de reparación/rebuild.

La decisión es correcta para QuickStay, pero **CQRS no debe aplicarse por moda**: para un CRUD pequeño con pocas lecturas, el coste operativo normalmente no compensa.

---

## Separación de persistencia CQRS

La instancia PostgreSQL de QuickStay contiene dos schemas con responsabilidades distintas:

```text
quickstay
├── quickstay_write
│   ├── hotels
│   ├── rooms
│   ├── guests
│   ├── reservations
│   └── saga_executions
│
└── quickstay_read
    ├── room_availability_read_model
    └── reservation_availability_read_model
```

`quickstay_write` es la fuente de verdad transaccional. `quickstay_read` es una
proyección diseñada específicamente para consultas de disponibilidad.

La base `quickstay_payment` pertenece al Payment Service y representa
Database-per-Service; no se presenta como parte del CQRS.

### Regla de acceso

```text
COMMAND → quickstay_write
EVENT   → RabbitMQ
PROJECT → quickstay_read
QUERY   → quickstay_read
```

## Cómo verificar la actividad

### 1. Construir el backend

Desde `infra/`:

```bash
docker compose up -d --build backend
```

No es necesario bajar todo el stack si el resto ya está funcionando.

### 2. Comprobar las colas

Abrir RabbitMQ Management:

```text
http://localhost:15672
```

Debe aparecer, entre otras, la cola:

```text
inventory.room-availability-projection
```

con un consumidor activo cuando `quickstay-backend` está levantado.

### 3. Ejecutar una búsqueda

La UI continúa usando:

```text
Frontend :4200
    ↓
Gateway :8000
    ↓
Backend :8082
    ↓
Read Model
```

La consulta ya no ejecuta la query JPA que cruza `Room` con `Reservation`. El `RoomAvailabilityReadRepository` consulta directamente las tablas de lectura.

### 4. Crear una reserva

Al confirmar una reserva, revisar los logs del backend:

```text
Publishing RESERVATION_CONFIRMED ...
[CQRS] Read model actualizado: reservation=... status=CONFIRMED room=...
```

Después de que el consumidor procese el evento, repetir la búsqueda para las mismas fechas. La habitación debe dejar de aparecer.

### 5. Probar compensación del Saga

Con el checkbox de fallo de pago activado en la UI, ejecutar una reserva Saga.

El flujo esperado es:

```text
PENDING_PAYMENT
      ↓
payment failure
      ↓
CANCELLED
      ↓
CQRS projection
      ↓
room available again
```

La cola de Inventory debe procesar primero el bloqueo temporal y después la cancelación.

---

## Decisiones de diseño

### ¿Por qué no crear dos microservicios?

Porque la actividad pide separar modelos de lectura y escritura, no necesariamente separar deployables. El backend todavía tiene suficiente cohesión para mantener Inventory y Booking dentro del mismo proceso, mientras que CQRS deja preparado el siguiente paso de extracción.

### ¿Por qué no usar una base de datos de lectura separada?

Porque introducir una segunda infraestructura persistente aumenta el coste operativo y no es necesario para demostrar CQRS. Las tablas son modelos lógicos distintos aunque, en esta iteración, compartan PostgreSQL.

### ¿Por qué RabbitMQ para la proyección?

Ya existe en QuickStay desde Sesión IV y representa un mecanismo natural para propagar los cambios del write model de forma asíncrona. Reutilizarlo evita introducir otro broker solo para la actividad.

### ¿Por qué mantener la validación de disponibilidad en el write model?

Porque el read model es eventualmente consistente y nunca debe convertirse en la única defensa contra overbooking. El write model sigue siendo la autoridad para una operación crítica de escritura.

### ¿Qué queda para una evolución futura? (No se solicita en la actividad)

- Read model en una base independiente.
- Rebuild de proyecciones desde un event log durable.
- Observabilidad de lag de proyección.
