# Sesión III — Structural Variation Styles

## Actividad 1: Architectural review of the initial monolith

### Hallazgos sobre el Layered Monolith (Sesión II)

1. **Acoplamiento por relación JPA cross-dominio.**
   `Reservation` (conceptualmente parte del negocio "Booking") tenía una
   relación `@ManyToOne` directa hacia `Room` (conceptualmente parte de
   "Inventory"). Esto significa que el agregado `Reservation` cargaba —o
   podía cargar, vía lazy loading— el grafo completo de `Room` → `Hotel`.
   Cualquier cambio en el modelo de `Room` (agregar un campo, cambiar un
   tipo) tenía potencial de romper código de reservas sin relación de
   negocio directa con ese cambio.

2. **Sin límites de Bounded Context.** Los paquetes `domain/`, `service/`,
   `repository/`, `controller/` agrupaban por **capa técnica**, no por
   **dominio de negocio**. Para entender "todo lo que pasa cuando alguien
   reserva una habitación" había que saltar entre 4 carpetas distintas,
   mezcladas con código de búsqueda de disponibilidad que es un caso de uso
   completamente distinto.

3. **Query cruzada Inventory → Booking.** `RoomRepository.findAvailableRooms`
   hacía una subquery JPQL directa contra la tabla `reservations` para
   calcular disponibilidad. Esto es coherente en un monolito con una sola
   base de datos, pero es exactamente el tipo de dependencia que **no**
   sobrevive a una futura extracción a microservicios (Sesión VI en
   adelante), porque ahí cada servicio tendría su propia base de datos
   aislada.

4. **Riesgo de rendimiento no cambia, pero el radio de impacto de un bug sí.**
   Con capas técnicas, un desarrollador tocando "reservas" podía
   accidentalmente romper "búsqueda" con más facilidad, porque ambos casos
   de uso comparten literalmente las mismas carpetas (`service/`,
   `repository/`). Con dominios separados, ese radio de impacto se reduce.

---

## Actividad 2: Refactor the code into strict domain packages/namespaces

### Decisión: dos Bounded Contexts

Con la funcionalidad implementada hasta ahora, identificamos 2 dominios
claros (los demás — Payment, Notification, Loyalty, CheckIn — todavía no
tienen código, se agregan en sesiones futuras):

| Dominio | Responsabilidad | Paquete |
|---|---|---|
| **Inventory** | Hoteles, habitaciones, búsqueda de disponibilidad | `com.quickstay.inventory` |
| **Booking** | Huéspedes, reservas, cancelaciones | `com.quickstay.booking` |
| **Shared** | Infraestructura cross-cutting (manejo global de errores) | `com.quickstay.shared` |

Cada dominio replica internamente la estructura por capas de la Sesión II
(`domain/`, `repository/`, `service/`, `web/`, `dto/`), pero ahora **como
subpaquete de un dominio**, no como paquete raíz del proyecto. Esto es
consciente: seguimos en un monolito (una sola base de datos, un solo
deployable), pero el código ya está organizado como si fuera a separarse en
servicios independientes más adelante — que es literalmente el objetivo de
un *Modular Monolith*.

### Cambio clave: `Reservation.room` → `Reservation.roomId`

Este es el cambio más importante del refactor, más allá de mover archivos
de carpeta:

**Antes (Sesión II):**
```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "room_id")
private Room room;
```

**Ahora (Sesión III):**
```java
@Column(name = "room_id", nullable = false)
private UUID roomId;
```

`Booking` ya no mapea `Room` como parte de su propio grafo de persistencia.
Solo conoce su identificador. La columna física `room_id` en la tabla
`reservations` no cambió — el FK a nivel de base de datos sigue existiendo
— pero a nivel de **código Java**, el dominio Booking ya no puede navegar
"reservation.getRoom().getHotel().getName()" por accidente. Si necesita
datos de la habitación, tiene que pedírselos explícitamente al dominio
Inventory (hoy vía `RoomRepository`, mañana vía una llamada HTTP o un
evento si Inventory se convierte en su propio servicio).

### Acoplamiento que queda pendiente (a propósito)

Dos puntos de acoplamiento cross-dominio siguen existiendo, documentados
como decisión consciente y no como descuido:

1. `booking.service.ReservationService` depende de
   `inventory.repository.RoomRepository` para validar que la habitación
   existe antes de reservar.
2. `inventory.repository.RoomRepository.findAvailableRooms` hace una
   subquery JPQL contra la entidad `Reservation` (dominio Booking) para
   calcular disponibilidad.

Ambos son aceptables en un *Modular Monolith* con una única base de datos
compartida. **No** serían aceptables si Inventory y Booking fueran
microservicios con bases de datos separadas — en ese escenario:
- El punto 1 se resolvería con una llamada síncrona (REST/gRPC) o
  aceptando el riesgo y validando solo al momento de guardar (compensación
  si falla).
- El punto 2 se resolvería manteniendo una **proyección de disponibilidad**
  dentro de Inventory, actualizada por eventos que Booking publica al
  confirmar/cancelar una reserva — el patrón que se construye en la
  **Sesión IV** (Enterprise Integration & Messaging) y se refina con
  **CQRS** en la **Sesión VIII**.

### Qué NO cambió en esta sesión

- El schema de PostgreSQL (mismas tablas, mismas columnas).
- Los endpoints REST (`/api/rooms/search`, `/api/reservations`).
- El frontend Angular.
- Sigue siendo **un solo deployable** — todavía no hay separación en
  procesos/servicios independientes. Eso es Sesión VI en adelante.
