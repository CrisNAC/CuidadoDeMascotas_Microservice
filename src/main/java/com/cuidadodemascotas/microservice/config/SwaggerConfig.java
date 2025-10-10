package com.cuidadodemascotas.microservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI para documentación de la API
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cuidado De Mascota - Reservation Service API")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión de reservaciones y servicios de reservación.
                                
                                **Características principales:**
                                - Gestión completa de Reservations (CRUD + Paginación + Filtros)
                                - Gestión completa de ReservationServices (CRUD + Paginación + Filtros)
                                - Borrado lógico (soft delete)
                                - Validaciones de negocio
                                - Logs detallados en todas las capas
                                
                                **Tecnologías:**
                                - Spring Boot 3.5.6
                                - Java 21
                                - PostgreSQL
                                - Spring Data JPA
                                - Liquibase
                                """)
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("desarrollo@cuidadodemascotas.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + contextPath)
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.cuidadodemascotas.com" + contextPath)
                                .description("Servidor de Producción")
                ));
    }
}