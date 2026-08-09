# Sesión VII — Advanced Distributed Architectures: API Gateway

## Actividad: Deploy an API Gateway

**Objetivo:** un único punto de entrada para los clientes (el frontend
Angular), que enruta transparentemente hacia el monolito restante
(`quickstay-backend`) o hacia el microservicio extraído en Sesión VI
(`quickstay-payment-service`), sin que el cliente tenga que saber cuál de
los dos atiende cada request.

## Por qué Spring Cloud Gateway

Se eligió Spring Cloud Gateway (en vez de, por ejemplo, un proxy manual con
`RestTemplate`, o Nginx) porque:
- Es el gateway "nativo" del ecosistema Spring — coherente con el resto del
  stack del proyecto.
- Trae soporte de primera clase para Circuit Breaker (Resilience4j) como
  filtro de ruta, que es exactamente el patrón que pide la bibliografía de
  esta sesión (Davis, Cap. 10 — *Fronting Services: Circuit Breakers and
  API Gateways*).

## YAML vs. Java para definir las rutas

La primera versión de este módulo definía las rutas en
`spring.cloud.gateway.routes` dentro de `application.yml`. Se cambió a un
`RouteLocator` en código Java (`GatewayRoutesConfig`) por una razón
concreta: **el binding de configuración YAML de Spring Cloud Gateway solo
se valida en tiempo de ejecución** — un predicate mal escrito, una
indentación incorrecta, o una versión de Spring Cloud con un cambio sutil
en el parsing, pueden hacer que una ruta simplemente no se registre, sin
ningún error visible al arrancar. Con el DSL Java, el compilador valida
cada `.path(...)`, `.filters(...)` y `.uri(...)` antes de que el código
llegue a ejecutarse — mucho más seguro para un proyecto académico donde
lo importante es poder confiar en que, si compila, las rutas existen.

## Topología resultante

```
Frontend (4200)
      │
      ▼
API Gateway (8000)  ← único punto de entrada público
      │
      ├── /api/rooms/**        → quickstay-backend (8080/8082)
      ├── /api/reservations/** → quickstay-backend (8080/8082)
      ├── /api/sagas/**        → quickstay-backend (8080/8082)
      └── /api/payments/**     → quickstay-payment-service (8081)  [+ Circuit Breaker]
```

El frontend deja de conocer que existen dos deployables distintos detrás —
antes de esta sesión, `environment.ts` apuntaba directo a `localhost:8080`
(el backend). Ahora apunta a `localhost:8000` (el Gateway), y es el Gateway
quien decide, según el path, a cuál de los dos servicios reenviar. Esto es
justamente lo que hace posible seguir extrayendo microservicios en el
futuro (ej. separar Inventory) sin que el frontend tenga que cambiar nada.

## Por qué el Saga NO pasa por el Gateway para llamar a Payment

`SagaOrchestrator` (dentro de `quickstay-backend`) sigue llamando a
`payment-service` directo, vía `PaymentServiceClient` con la URL de
`PAYMENT_SERVICE_URL` — **no** a través del Gateway. Esto es intencional y
es una distinción arquitectónica importante:

- El **API Gateway** resuelve el problema de *cliente externo → sistema*
  (norte-sur): el frontend, o cualquier consumidor externo, no debería
  necesitar saber la topología interna de servicios.
- La llamada del Saga a Payment es *servicio → servicio* (este-oeste),
  interna al sistema. Meterla a través del Gateway agregaría un salto de
  red innecesario y acoplaría la comunicación interna a la disponibilidad
  del Gateway sin ningún beneficio real — el Gateway no aporta nada a una
  llamada que ya sabe exactamente a quién llamar.

En una arquitectura de microservicios madura, este tipo de comunicación
interna normalmente se resuelve con Service Discovery (Sesión VIII toca
tangencialmente este tema) en vez de URLs fijas — pero ese es un problema
distinto al que resuelve un API Gateway.

## Circuit Breaker en la ruta de Payment — y su relación con el Saga

Se agregó un Circuit Breaker (Resilience4j) **solo** en la ruta
`/api/payments/**`, no en las rutas hacia el monolito. Motivo: Payment es
el componente más nuevo, con su propio ciclo de vida y su propia base de
datos — es razonablemente más probable que esté caído o lento de forma
aislada (a diferencia del monolito, que si se cae, se cae todo el sistema
igual).

Es importante notar que este Circuit Breaker protege una llamada
**directa** del cliente a `/api/payments/**` (por ejemplo, si alguna
herramienta administrativa llamara a ese endpoint directamente a través
del Gateway) — es un mecanismo de resiliencia **distinto y en una capa
distinta** al patrón de compensación del Saga (Sesión V):

| Mecanismo | Capa | Qué protege |
|---|---|---|
| Circuit Breaker del Gateway | Cliente ↔ Gateway ↔ Payment | Evita que un cliente HTTP espere indefinidamente si Payment está caído |
| Compensación del Saga | Backend ↔ Payment (interno) | Garantiza que, si el pago fue rechazado (no caído, sino rechazado), la reserva se revierta y la habitación se libere |

Ambos son necesarios — resuelven fallas de naturaleza distinta (Payment
*caído* vs. Payment *funcionando pero rechazando el pago*).

## Qué NO se hizo en esta sesión (a propósito)

- **Autenticación/autorización en el Gateway**: quedaría fuera de alcance
  del módulo; en un sistema real, el Gateway sería también el lugar natural
  para validar JWT antes de reenviar el request.
- **Rate limiting**: Spring Cloud Gateway lo soporta nativamente
  (`RequestRateLimiter` filter), pero no se agregó para mantener el alcance
  acotado a lo que pide la actividad.
- **Service Discovery dinámico** (Eureka/Consul): las URLs de los servicios
  siguen siendo fijas por configuración (`BACKEND_SERVICE_URL`,
  `PAYMENT_SERVICE_URL`). Es la aproximación correcta para 2 servicios;
  con más microservicios, un registro de servicios sería necesario.

## Cómo probarlo

1. Todo arriba: `docker compose up -d` desde `infra/` (o cada módulo con
   `./gradlew bootRun` en su propia terminal — ver README).
2. El frontend (`http://localhost:4200`) ahora habla con
   `http://localhost:8000` — confirmalo en las DevTools del navegador
   (pestaña Network): las requests deberían ir al puerto 8000, no al 8080.
3. Probá el Circuit Breaker manualmente: apagá `payment-service`
   (`docker stop quickstay-payment-service` o Ctrl+C si lo corrés local) y
   hacé una reserva con pago desde el frontend. Después de los reintentos
   configurados, el Gateway debería responder `503` con el mensaje de
   `FallbackController`, en vez de dejar la request colgada.
4. Revisá el estado del Circuit Breaker en
   `http://localhost:8000/actuator/circuitbreakers` (requiere que
   `payment-service` haya fallado al menos `minimum-number-of-calls` veces
   para que el breaker cambie de estado).
