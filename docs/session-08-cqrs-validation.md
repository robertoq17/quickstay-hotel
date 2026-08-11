# Sesión VIII — Validación y demostración de CQRS

## 1. Separación de persistencia

QuickStay PostgreSQL contiene dos esquemas lógicos de CQRS:

```text
quickstay
├── quickstay_write
│   ├── hotels
│   ├── rooms
│   ├── guests
│   ├── reservations
│   └── saga_executions
└── quickstay_read
    ├── room_availability_read_model
    └── reservation_availability_read_model
```

`quickstay_write` es la fuente autorizada para las operaciones transaccionales de escritura. `quickstay_read` es una proyección desnormalizada utilizada para las consultas de disponibilidad.

La base de datos PostgreSQL independiente `quickstay_payment` corresponde al patrón **Database-per-Service** y no se utiliza como evidencia de CQRS.

## 2. Validar los esquemas

Desde pgAdmin o `psql`:

```sql
SELECT schema_name
FROM information_schema.schemata
WHERE schema_name IN ('quickstay_write', 'quickstay_read');

SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema IN ('quickstay_write', 'quickstay_read')
ORDER BY table_schema, table_name;
```

## 3. Validar el flujo de lectura

```text
Angular
  ↓
API Gateway :8000
  ↓
RoomSearchController
  ↓
RoomSearchService
  ↓
RoomAvailabilityReadRepository
  ↓
quickstay_read
```

La búsqueda de disponibilidad **no debe utilizar** `RoomRepository` ni `ReservationRepository`.

## 4. Validar el flujo de escritura y eventos

```text
Reservation/Saga
  ↓
quickstay_write
  ↓
Evento de integración AFTER_COMMIT
  ↓
RabbitMQ
  ↓
inventory.room-availability-projection
  ↓
InventoryAvailabilityProjectionListener
  ↓
quickstay_read
```

## 5. Demostración: fallo de RabbitMQ

Detener únicamente el broker:

```bash
docker compose stop rabbitmq
```

Observar el backend y la interfaz de administración de RabbitMQ. El lado **WRITE** mantiene su propia persistencia de forma independiente, mientras que la sincronización asíncrona de la proyección **READ** se interrumpe.

Reiniciar:

```bash
docker compose start rabbitmq
docker compose logs -f backend
```

Verificar la cola y el consumidor en:

`http://localhost:15672`

> Las colas durables de RabbitMQ pueden conservar los mensajes que ya fueron aceptados por el broker.
> CQRS por sí mismo no garantiza la recuperación de un evento que nunca llegó a RabbitMQ.

## 6. Demostración: fallo de Payment

Detener únicamente el servicio de Payment:

```bash
docker compose stop payment-service
```

Ejecutar un flujo de reserva/pago y observar el comportamiento de **Saga/Circuit Breaker**.

Restaurar:

```bash
docker compose start payment-service
```

## 7. Inicialización limpia después de los cambios

Si el volumen de Docker fue creado por una versión anterior de QuickStay, ejecutar una sola vez:

```bash
docker compose down -v
docker compose up -d
```

No repetir `down -v` para cambios normales de código, ya que elimina los datos locales de las bases de datos.

