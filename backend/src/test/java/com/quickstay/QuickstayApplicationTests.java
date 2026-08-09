package com.quickstay;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
class QuickstayApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test: verifica que el contexto de Spring levanta correctamente.
        // Requiere PostgreSQL Y RabbitMQ corriendo (ver infra/docker-compose.yml),
        // porque los @RabbitListener arrancan sus contenedores de consumo al
        // iniciar el contexto (autoStartup=true por defecto).
    }
}
