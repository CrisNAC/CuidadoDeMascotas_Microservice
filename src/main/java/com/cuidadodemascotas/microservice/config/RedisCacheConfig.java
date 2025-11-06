package com.cuidadodemascotas.microservice.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // 👈 Soporte para fechas Java 8
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);

        // Serializador JSON que usa el ObjectMapper configurado
        RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Configuración base global
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL global (10 minutos)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // Configuración específica por dominio
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Reservas (TTL 15 min)
        cacheConfigurations.put("reservations",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(15)).prefixCacheNameWith("reservations::"));

        // Servicios de reserva (TTL 30 min)
        cacheConfigurations.put("reservation_services",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(30)).prefixCacheNameWith("reservationServices::"));

        // Usuarios de reserva (TTL 10 min)
        cacheConfigurations.put("reservation_users",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(10)).prefixCacheNameWith("reservationUsers::"));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
