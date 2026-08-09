package com.quickstay.gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Respuesta que el Gateway devuelve cuando el Circuit Breaker de
 * payment-service está abierto (el servicio está caído, lento, o viene
 * fallando por encima del umbral configurado en application.yml).
 *
 * Importante: esto NO reemplaza la lógica de compensación del Saga (Sesión
 * V) — son dos mecanismos de resiliencia en capas distintas. El Circuit
 * Breaker del Gateway protege al cliente HTTP externo de esperar
 * indefinidamente; la compensación del Saga protege la consistencia de los
 * datos cuando el pago sí llega a ejecutarse pero falla. Ver
 * docs/session-07-evaluation.md.
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback/payments")
    public Mono<ResponseEntity<Map<String, Object>>> paymentServiceFallback() {
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Unavailable",
                "message", "Payment Service no está disponible en este momento. "
                        + "Intentá de nuevo en unos segundos — la reserva NO se " +
                        "confirma hasta que el pago se procese correctamente."
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }
}
