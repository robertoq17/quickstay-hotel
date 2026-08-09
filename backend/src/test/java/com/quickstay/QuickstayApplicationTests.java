package com.quickstay;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
class QuickstayApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test: verifica que el contexto de Spring levanta correctamente.
        // Requiere PostgreSQL corriendo (ver infra/docker-compose.yml).
    }
}
