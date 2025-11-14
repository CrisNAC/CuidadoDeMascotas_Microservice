package com.cuidadodemascotas.microservice.config;

import com.cuidadodemascotas.microservice.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // Usa tu CorsConfig
                .authorizeHttpRequests(auth -> auth
                        // ========== SWAGGER Y OPENAPI ==========
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ========== ACTUATOR Y EUREKA ==========
                        .requestMatchers("/actuator/**").permitAll()

                        // ========== ENDPOINTS PUBLICOS (CONSULTA) ==========
                        /*.requestMatchers(
                                "/service-types/**",      // GET público
                                "/services",              // GET público
                                "/services/{id}",         // GET público
                                "/services/search"        // GET público
                        ).permitAll()*/

                        // ========== TODO LO DEMAS REQUIERE AUTENTICACION ==========
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Manejo de 401 para requests sin token o no autenticados
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (request, response, authException) -> {
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                                }
                        )
                );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
