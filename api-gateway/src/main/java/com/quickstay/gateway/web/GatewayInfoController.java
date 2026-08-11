package com.quickstay.gateway.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * El Gateway solo tiene rutas configuradas para /api/** (ver
 * GatewayRoutesConfig). Sin este endpoint, entrar a http://localhost:8000/
 * directamente devuelve 404 por diseño — lo cual es fácil de confundir con
 * "el Gateway no tiene nada configurado". Este endpoint es solo un chequeo
 * de vida rápido para humanos, no forma parte del enrutamiento real.
 */
@RestController
public class GatewayInfoController {

    @GetMapping("/")
    public Mono<Map<String, Object>> info() {
        return Mono.just(Map.of(
                "service", "quickstay-api-gateway",
                "status", "UP",
                "routes", Map.of(
                        "/api/rooms/**", "-> quickstay-backend",
                        "/api/reservations/**", "-> quickstay-backend",
                        "/api/sagas/**", "-> quickstay-backend",
                        "/api/payments/**", "-> quickstay-payment-service (con Circuit Breaker)"
                ),
                "actuator", "/actuator/gateway/routes para el detalle real de rutas registradas"
        ));
    }
}
