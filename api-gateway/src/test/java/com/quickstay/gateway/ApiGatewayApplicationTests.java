package com.quickstay.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test: verifica que el contexto reactivo del Gateway levanta
        // correctamente y las rutas declaradas en application.yml son
        // válidas. No requiere que backend/payment-service estén corriendo
        // para levantar el contexto (las rutas se resuelven en tiempo de
        // request, no al arrancar).
    }
}
