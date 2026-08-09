package com.quickstay.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS cross-cutting (dominio "shared", no pertenece a
 * Inventory ni a Booking).
 *
 * En dev, el frontend Angular corre en http://localhost:4200 y el backend
 * en http://localhost:8080 — son orígenes distintos para el navegador, así
 * que sin esta configuración el browser bloquea las respuestas del API
 * aunque el request HTTP se complete bien en el servidor (por eso el log
 * del backend no muestra error: el bloqueo lo hace el navegador, no Spring).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
