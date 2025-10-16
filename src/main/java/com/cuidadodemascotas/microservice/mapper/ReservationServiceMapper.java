package com.cuidadodemascotas.microservice.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cuidadodemascota.commons.dto.ReservationServiceRequestDTO;
import org.example.cuidadodemascota.commons.dto.ReservationServiceResponseDTO;
import org.example.cuidadodemascota.commons.entities.reservation.Reservation;
import org.example.cuidadodemascota.commons.entities.reservation.ReservationService;
import org.example.cuidadodemascota.commons.entities.service.Service;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Mapper manual para conversión entre ReservationService Entity y DTOs
 * Esta es la entidad intermedia de la relación muchos-a-muchos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationServiceMapper implements IBaseMapper<ReservationService, ReservationServiceRequestDTO, ReservationServiceResponseDTO>{

    /**
     * Convierte Request DTO a Entity (para creación)
     * IMPORTANTE: NO setea las relaciones aquí, solo crea la entidad vacía
     * Las relaciones (reservation y service) se setean en el servicio con setRelations()
     */
    public ReservationService toEntity(ReservationServiceRequestDTO dto) {
        log.debug("Convirtiendo ReservationServiceRequestDTO a Entity");

        if (dto == null) {
            log.warn("DTO recibido es null");
            return null;
        }

        // Solo creamos la entidad vacía
        // Las relaciones se configuran después en el Service
        ReservationService entity = new ReservationService();

        log.debug("Entity ReservationService creada (sin relaciones aún)");
        return entity;
    }

    /**
     * Convierte Entity a Response DTO
     * Solo mapea IDs de las relaciones para evitar lazy loading
     */
    public ReservationServiceResponseDTO toDto(ReservationService entity) {
        log.debug("Convirtiendo ReservationService Entity a ResponseDTO");

        if (entity == null) {
            log.warn("Entity recibida es null");
            return null;
        }

        ReservationServiceResponseDTO dto = new ReservationServiceResponseDTO();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt() != null
                ? entity.getCreatedAt().atOffset(ZoneOffset.of("-03:00"))
                : null);

        dto.setUpdatedAt(entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().atOffset(ZoneOffset.of("-03:00"))
                : null);

        dto.setActive(entity.getActive());

        // Mapeo de relaciones - Solo IDs
        if (entity.getReservation() != null) {
            dto.setReservationId(Math.toIntExact(entity.getReservation().getId()));
        }
        if (entity.getService() != null) {
            dto.setServiceId(Math.toIntExact(entity.getService().getId()));
        }

        log.debug("ResponseDTO creado: id={}, reservationId={}, serviceId={}",
                dto.getId(), dto.getReservationId(), dto.getServiceId());

        return dto;
    }

    /**
     * Configura las relaciones de la entity (Reservation y Service)
     * Se llama desde el servicio después de buscar las entidades relacionadas
     */
    public void setRelations(ReservationService entity, Reservation reservation, Service service) {
        log.debug("Configurando relaciones para ReservationService");

        if (reservation != null) {
            entity.setReservation(reservation);
            log.debug("Reservation configurada: ID={}", reservation.getId());
        }
        if (service != null) {
            entity.setService(service);
            log.debug("Service configurado: ID={}", service.getId());
        }
    }
}