package com.cuidadodemascotas.microservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String SECRET_KEY;

    /**
     * Obtener la clave de firma
     */
    private Key getKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extraer el username (email) del token
     */
    public String extractUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Obtener todas las claims del token
     */
    public Claims getAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Obtener un claim específico
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extraer roles del token (formato: "ROLE_CARER,ROLE_OWNER")
     */
    public List<String> extractRoles(String token) {
        String rolesString = getClaim(token, claims -> claims.get("roles", String.class));
        if (rolesString == null || rolesString.isBlank()) {
            return List.of();
        }
        return Arrays.asList(rolesString.split(","));
    }

    /**
     * Validar si el token es válido (no expirado)
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verificar si el token ha expirado
     */
    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    /**
     * Obtener la fecha de expiracion
     */
    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }
}
