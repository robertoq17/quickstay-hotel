# QuickStay Hotel Platform

## 🎯 Objetivo de la kata

Desarrollar una plataforma de reservas hoteleras que permita buscar disponibilidad,
reservar, pagar, gestionar check-in/out digital y recibir promociones — evolucionando
progresivamente su arquitectura a lo largo de las 8 sesiones del módulo
*Microservices, Event-Driven and Cloud-Native*.

---

# 1era Actividad — Choose a Software Architecture Kata

**QuickStay Hotel Platform**

## Description

A hotel group wants to create a booking platform for its hotels. Customers can search
availability, reserve rooms, pay online, check in digitally, request services, and
receive promotions.

## Users

Potentially millions of travelers across regions.

## Requirements

* Allow users to search rooms by location, date, price, and amenities.
* Allow users to reserve a room.
* Allow users to pay online or at the hotel.
* Allow users to cancel or modify reservations.
* Support loyalty points.
* Support national promotions.
* Support hotel-specific promotions.
* Allow digital check-in and check-out.
* Allow users to request room service, housekeeping, or maintenance.
* Notify customers about booking confirmation, reminders, and changes.
* Integrate with external travel agencies.
* Integrate with payment providers.
* Support mobile access.

## Additional Context

* Some hotels are owned by the company; others are franchises.
* Room availability changes constantly.
* Third-party booking platforms may reserve rooms too.
* Overbooking must be avoided or carefully managed.
* Cancellations may have different policies.
* Future expansion includes multiple countries and currencies.

---

# 2da Actividad — Build the Monolith Core (Sesión II)

Se construyó un **monolito en capas** puro (separado por rol técnico:
presentation → service → repository → domain), sin separación por dominio
de negocio todavía. Ese fue el punto de partida intencional — ver el
refactor hacia dominios en la Sesión III, más abajo.

### Alcance funcional (sigue vigente)

* ✅ Búsqueda de disponibilidad por ciudad, fechas y precio máximo
* ✅ Reserva de habitación con validación de solapamiento (anti-overbooking
básico, dentro de una única transacción)
* ✅ Cancelación de reserva
* ✅ Alta automática de huésped al reservar
* ⏳ Pago online, loyalty, promociones, check-in digital, notificaciones,
integración con OTAs → planificado para sesiones posteriores

---

# 3ra Actividad — Structural Variation Styles (Sesión III)

**Actividades:** *Conduct an architectural review of the initial monolith*

* *Refactor the code into strict domain packages/namespaces*.

Ver el detalle completo de la revisión y las decisiones de diseño en
[`docs/session-03-evaluation.md`](docs/session-03-evaluation.md).

## 🏛️ Arquitectura actual: Modular Monolith (Bounded Contexts)

El código se reorganizó de capas técnicas a **paquetes por dominio de
negocio**. Sigue siendo un único deployable con una única base de datos
(todavía no es microservicios), pero cada dominio ya es internamente
cohesivo y sus límites están explícitos en el código.

```
quickstay-hotel-platform/
├── backend/                          Spring Boot (Java 17, Gradle Groovy)
│   └── src/main/java/com/quickstay/
│       ├── inventory/                Bounded Context: Hoteles y habitaciones
│       │   ├── domain/       Hotel, Room, HotelOwnershipType
│       │   ├── repository/   HotelRepository, RoomRepository
│       │   ├── service/      RoomSearchService
│       │   ├── web/          RoomSearchController
│       │   └── dto/          RoomSearchRequest, RoomAvailabilityResponse
│       │
│       ├── booking/                  Bounded Context: Huéspedes y reservas
│       │   ├── domain/       Guest, Reservation, ReservationStatus
│       │   ├── repository/   GuestRepository, ReservationRepository
│       │   ├── service/      ReservationService
│       │   ├── web/          ReservationController
│       │   ├── dto/          ReservationRequest, ReservationResponse
│       │   └── exception/    RoomNotAvailableException
│       │
│       └── shared/                   Cross-cutting
│           └── exception/    GlobalExceptionHandler
│
├── frontend/quickstay-web/           Angular 18 (standalone components) — sin cambios
├── infra/                            docker-compose (PostgreSQL) — sin cambios
└── docs/
    └── session-03-evaluation.md      Revisión arquitectónica + justificación del refactor
```

### Cambio de diseño clave

`Reservation` ya **no** mapea `Room` como relación JPA (`@ManyToOne`) —
ahora guarda solo `roomId: UUID`. Esto evita que el dominio Booking cargue
por accidente el grafo de entidades de Inventory, preservando el límite del
Bounded Context aunque ambas tablas sigan en la misma base de datos física.
Detalle completo en `docs/session-03-evaluation.md`.

### Principios de esta sesión

* **Bounded Contexts explícitos en el código**, no solo en la cabeza del
equipo.
* **Un solo deployable y una sola base de datos** — la modularización es de
código, todavía no de infraestructura (eso empieza en Sesión VI).
* Acoplamiento cross-dominio que queda **documentado como decisión
consciente**, no eliminado del todo (se resuelve con eventos en Sesión IV
y CQRS en Sesión VIII).

---

## 📦 Tecnologías

|Capa|Tecnología|
|-|-|
|**Backend**|Java 17, Spring Boot 3.3.2, Gradle 8.8 (Groovy DSL), Spring Data JPA, Flyway, Lombok, PostgreSQL driver|
|**Frontend**|Angular 18 (standalone components), TypeScript, SCSS|
|**Base de datos**|PostgreSQL 16 (Docker)|
|**Gestión de dependencias**|Gradle (backend) \& npm (frontend)|
|**Control de versiones**|Git — rama + tag por sesión|

---

# 🏨 Arquitectura de Software (C4 Model)

## 📌 Nivel 1: Diagrama de Contexto

```mermaid
graph TD
    traveler["👤 Viajero / Cliente<br/><i>[Persona]</i><br/>Busca habitaciones, reserva, paga online, gestiona check-in/out y solicita servicios."]
    hotelStaff["👤 Personal del Hotel<br/><i>[Persona]</i><br/>Atiende solicitudes de room service, limpieza y mantenimiento."]

    quickstay["🏨 QuickStay Platform<br/><i>[Sistema de Software]</i><br/>Gestiona disponibilidad, reservas, cancelaciones, fidelidad, promociones y solicitudes de huéspedes."]

    otas["🌐 Agencias de Viajes Externas (OTAs)<br/><i>[Sistema Externo]</i><br/>Consultan disponibilidad y reservan en hoteles franquiciados o propios."]
    paymentSystem["💳 Proveedores de Pago<br/><i>[Sistema Externo]</i><br/>Pasarelas de pago para transacciones en múltiples monedas."]
    notificationService["✉️ Servicio de Notificaciones<br/><i>[Sistema Externo]</i><br/>Email/SMS/Push para confirmaciones y alertas."]

    traveler -->|"Busca, reserva, paga, check-in digital, solicita servicios"| quickstay
    hotelStaff -->|"Gestiona solicitudes operativas"| quickstay
    otas -->|"Consulta disponibilidad y reserva"| quickstay
    quickstay -->|"Procesa cobros"| paymentSystem
    quickstay -->|"Envía notificaciones"| notificationService

    classDef person fill:#08427b,color:#fff,stroke:#073b6f,stroke-width:2px;
    classDef system fill:#1168bd,color:#fff,stroke:#0e569e,stroke-width:2px;
    classDef external fill:#999999,color:#fff,stroke:#666666,stroke-width:2px;

    class traveler,hotelStaff person;
    class quickstay system;
    class otas,paymentSystem,notificationService external;
```

> Los sistemas externos (OTAs, pagos, notificaciones) representan el estado
> **objetivo** de QuickStay. En Sesión II todavía no hay integraciones reales
> con ellos — se agregan en sesiones posteriores (IV en adelante).

## 📦 Nivel 2: Diagrama de Contenedores

```mermaid
graph TD
    user["👤 Usuario / Cliente<br/><i>[Person]</i>"]

    subgraph SystemBoundary["QuickStay Hotel System — Sesión II"]
        frontend["📱 QuickStay Frontend<br/><i>[Container: Angular 18]</i><br/>Búsqueda y reserva de habitaciones."]
        backend["⚙️ QuickStay Backend<br/><i>[Container: Java 17, Spring Boot, Gradle]</i><br/>Layered Monolith."]
        database[("🐘 PostgreSQL<br/><i>[ContainerDb]</i><br/>hotels, rooms, guests, reservations.")]
    end

    user -->|"Usa [HTTP]"| frontend
    frontend -->|"Consume REST API [JSON/HTTP]"| backend
    backend -->|"JDBC"| database

    classDef person fill:#08427b,color:#fff,stroke:#073b6f,stroke-width:2px;
    classDef container fill:#1168bd,color:#fff,stroke:#0e569e,stroke-width:2px;
    classDef db fill:#336791,color:#fff,stroke:#205ba8,stroke-width:2px;

    class user person;
    class frontend,backend container;
    class database db;
```

## 🧩 Nivel 3: Diagrama de Componentes — Bounded Contexts (Sesión III)

```mermaid
graph TD
    frontend["📱 QuickStay Frontend<br/><i>[Angular]</i>"]
    database[("🐘 PostgreSQL")]

    subgraph Inventory["📦 Inventory Bounded Context"]
        invWeb["RoomSearchController"]
        invService["RoomSearchService"]
        invRepo["Hotel/RoomRepository"]
        invDomain["Hotel, Room"]
    end

    subgraph Booking["📅 Booking Bounded Context"]
        bookWeb["ReservationController"]
        bookService["ReservationService"]
        bookRepo["Guest/ReservationRepository"]
        bookDomain["Guest, Reservation (roomId: UUID)"]
    end

    subgraph Roadmap["🗺️ Bounded Contexts planificados (sesiones futuras)"]
        paymentMod["Payment<br/><i>Sesión IV/V</i>"]
        notifMod["Notification<br/><i>Sesión IV — eventos</i>"]
        loyaltyMod["Loyalty / Promotions<br/><i>Sesión VI/VIII</i>"]
        checkinMod["Digital Check-in<br/><i>Sesión VII</i>"]
    end

    frontend -->|"Busca disponibilidad"| invWeb
    frontend -->|"Crea/cancela reservas"| bookWeb

    invWeb --> invService --> invRepo --> invDomain
    bookWeb --> bookService --> bookRepo --> bookDomain

    invRepo -->|"JDBC"| database
    bookRepo -->|"JDBC"| database

    bookService -.->|"lee: ¿existe la room?<br/>(único acoplamiento hacia Inventory)"| invRepo
    invRepo -.->|"subquery cross-domain<br/>(acoplamiento pendiente, ver docs)"| bookDomain

    bookService -.->|"futuro: evento ReservationConfirmed"| notifMod
    bookService -.->|"futuro: solicita cobro"| paymentMod

    classDef container fill:#1168bd,color:#fff,stroke:#0e569e,stroke-width:2px;
    classDef component fill:#85bbf0,color:#000,stroke:#5d82a8,stroke-width:1px;
    classDef db fill:#336791,color:#fff,stroke:#205ba8,stroke-width:2px;
    classDef future fill:#dddddd,color:#555,stroke:#999999,stroke-width:1px,stroke-dasharray: 5 5;

    class frontend container;
    class database db;
    class invWeb,invService,invRepo,invDomain,bookWeb,bookService,bookRepo,bookDomain component;
    class paymentMod,notifMod,loyaltyMod,checkinMod future;
```

---

## 🚀 Cómo levantar el entorno local

### 1. Base de datos (PostgreSQL vía Docker)

> Nota: si ya tenés un PostgreSQL nativo corriendo en tu máquina (Windows/Mac),
> puede ocupar el puerto 5432. Este proyecto usa el puerto **5433** en el host
> para evitar ese conflicto (ver `infra/docker-compose.yml`).

```bash
cd infra
docker compose up -d
docker ps   # confirmar que "quickstay-postgres" está Up
```

Credenciales (definidas en `docker-compose.yml`): DB `quickstay`, user/pass
`quickstay`/`quickstay`.

### 2. Backend

```bash
cd backend
./gradlew bootRun        # Windows: .\\gradlew.bat bootRun
```

Al arrancar, Flyway crea el schema y carga datos demo automáticamente
(`V1\_\_init\_schema.sql`, `V2\_\_seed\_demo\_data.sql`). La API queda en
`http://localhost:8080`.

**Nota:** el proceso queda corriendo en foreground (no "termina" — la barra de
progreso de Gradle se queda fija, eso es normal). Confirmá que levantó bien
buscando en el log: `Started QuickstayApplication in X seconds`.

Endpoints disponibles:

```
GET  /api/rooms/search?city={city}\&checkIn={yyyy-MM-dd}\&checkOut={yyyy-MM-dd}\&maxPrice={decimal}
POST /api/reservations
POST /api/reservations/{id}/cancel
```

Ejemplo:

```
http://localhost:8080/api/rooms/search?city=La%20Paz\&checkIn=2026-09-01\&checkOut=2026-09-05\&maxPrice=600
```

### 3. Frontend

```bash
cd frontend/quickstay-web
npm install
npm start
```

Se levanta en `http://localhost:4200`, ya conectado al backend
(`src/environments/environment.ts`).

### 4. Verificar

* `http://localhost:4200` → formulario de búsqueda y listado de habitaciones
disponibles, con opción de reservar.
* `http://localhost:8080/api/rooms/search?...` → JSON con habitaciones (ver
ejemplo arriba).

---

## 🧪 Troubleshooting rápido

|Síntoma|Causa probable|Solución|
|-|-|-|
|`FATAL: la autentificación password falló`|Volumen de Postgres viejo con otras credenciales, o conflicto de puerto con un Postgres nativo|`docker compose down -v \&\& docker compose up -d`|
|`Unable to determine Dialect without JDBC metadata`|Backend no logra conectar a la DB (mensaje real de Hibernate queda oculto)|Verificar `docker ps` y el puerto en `application.yml`|
|Barra de Gradle se queda en 80-90%|Comportamiento normal de `bootRun` — el proceso queda vivo sirviendo peticiones|Buscar `Started QuickstayApplication` en el log|

---

## 🗂️ Git — flujo por sesión

Sesión III:

```bash
git checkout -b session-03-modular-monolith
git add .
git commit -m "refactor: reorganize into inventory/booking bounded contexts

- move layered packages into domain-oriented packages (inventory, booking)
- decouple Reservation from Room JPA relation, use roomId reference instead
- document remaining cross-domain coupling in docs/session-03-evaluation.md"
git push -u origin session-03-modular-monolith

git checkout main
git merge --no-ff session-03-modular-monolith -m "merge: session 03 modular monolith (bounded contexts)"
git tag -a v0.3-modular-monolith -m "Session III: Modular Monolith - Bounded Contexts"
git push origin main --tags
```

---

