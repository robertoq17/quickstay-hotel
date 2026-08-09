package com.quickstay.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Rutas del Gateway definidas en código, no en YAML.
 *
 * Se eligió el DSL Java (RouteLocatorBuilder) en vez de
 * `spring.cloud.gateway.routes` en application.yml porque acá el compilador
 * valida la sintaxis — con YAML, un typo en un predicate o un filtro recién
 * se descubre en tiempo de ejecución (o ni eso, si el binding falla en
 * silencio). Para una ruta con lógica adicional como el Circuit Breaker de
 * Payment, el DSL Java también es más legible que anidar filtros en YAML.
 */
@Configuration
public class GatewayRoutesConfig {

    @Value("${quickstay.backend.base-url:http://127.0.0.1:8080}")
    private String backendBaseUrl;

    @Value("${quickstay.payment.base-url:http://127.0.0.1:8081}")
    private String paymentBaseUrl;

    @Bean
    public RouteLocator quickstayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // --- Rutas hacia el MONOLITO (quickstay-backend) ---
                .route("inventory-route", r -> r
                        .path("/api/rooms/**")
                        .uri(backendBaseUrl))

                .route("booking-route", r -> r
                        .path("/api/reservations/**")
                        .uri(backendBaseUrl))

                .route("saga-route", r -> r
                        .path("/api/sagas/**")
                        .uri(backendBaseUrl))

                // --- Ruta hacia el MICROSERVICIO extraído (quickstay-payment-service) ---
                // Única ruta con Circuit Breaker: Payment es el componente
                // más nuevo y aislado (su propia DB, su propio deploy) —
                // patrón "Fronting Services" (Davis, Cap. 10).
                .route("payment-route", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.circuitBreaker(c -> c
                                .setName("paymentServiceCB")
                                .setFallbackUri("forward:/fallback/payments")))
                        .uri(paymentBaseUrl))

                .build();
    }
}
