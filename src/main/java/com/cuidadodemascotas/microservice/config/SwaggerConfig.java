package com.cuidadodemascotas.microservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI reservationMicroserviceOpenAPI() {

        // === Definición del servidor ===
        Server server = new Server();
        server.setUrl("/reservations");
        server.setDescription("Reservation Microservice Deployed Server");

        // === Contacto ===
        Contact contact = new Contact();
        contact.setName("Equipo de Desarrollo");
        contact.setEmail("desarrollo@cuidadodemascotas.com");

        // === Licencia ===
        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");

        // === Información del microservicio ===
        Info info = new Info()
                .title("Cuidado de Mascotas - Reservation Microservice API")
                .version("1.0.0")
                .description("""
                        Microservicio encargado de la gestión de reservaciones del sistema.

                        Funcionalidades principales:
                        - CRUD de Reservations
                        - CRUD de ReservationServices
                        - Borrado lógico (soft delete)
                        - Validaciones de negocio
                        - Logs en todas las capas
                        - Paginación y filtros avanzados
                        """)
                .contact(contact)
                .license(license);

        // === Retornar OpenAPI ===
        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
