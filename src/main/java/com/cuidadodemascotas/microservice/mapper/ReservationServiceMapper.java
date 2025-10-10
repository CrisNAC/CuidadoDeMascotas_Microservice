package com.cuidadodemascotas.microservice.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cuidadodemascota.commons.dto.ReservationServiceRequestDTO;
import org.example.cuidadodemascota.commons.dto.ReservationServiceResponseDTO;
import org.example.cuidadodemascota.commons.entities.reservation.ReservationService;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Mapper manual para conversión entre ReservationService Entity y DTOs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationServiceMapper {

    /**
     * Convierte Request DTO a Entity
     */
    public ReservationService toEntity(ReservationServiceRequestDTO dto) {
        log.debug("Convirtiendo ReservationServiceRequestDTO a Entity");

        if (dto == null) {
            log.warn("DTO recibido es null");
            return null;
        }

        ReservationService entity = new ReservationService();

        entity.setReservation(dto.getReservationId());
        entity.setService(dto.getServiceId());

        log.debug("Entity creada: quantity={}, subtotal={}",
                entity.getReservation(), entity.getService());

        return entity;
    }

    /**
     * Actualiza una entity existente con los datos del Request DTO
     */
    public void updateEntityFromDto(ReservationService entity, ReservationServiceRequestDTO dto) {
        log.debug("Actualizando ReservationService entity ID={} desde DTO", entity.getId());

        if (dto == null || entity == null) {
            log.warn("DTO o Entity es null");
            return;
        }

        if (dto.getReservationId() != null) {
            entity.setReservation(dto.getReservationId());
        }
        if (dto.getServiceId() != null) {
            entity.setService(dto.getServiceId());
        }

        log.debug("Entity actualizada: quantity={}, subtotal={}",
                entity.getReservation(), entity.getService());
    }

    /**
     * Convierte Entity a Response DTO
     */
    public ReservationServiceResponseDTO toResponseDTO(ReservationService entity) {
        log.debug("Convirtiendo ReservationService Entity a ResponseDTO");

        if (entity == null) {
            log.warn("Entity recibida es null");
            return null;
        }

        ReservationServiceResponseDTO dto = new ReservationServiceResponseDTO();
        dto.setId(entity.getId());
        dto.setReservationId(entity.getReservation());
        dto.setServiceId(entity.getService());
        dto.setCreatedAt(OffsetDateTime.from(entity.getCreatedAt()));
        dto.setUpdatedAt(OffsetDateTime.from(entity.getUpdatedAt()));
        dto.setActive(entity.getActive());

        log.debug("ResponseDTO creado: id={}, quantity={}, subtotal={}",
                dto.getId(), dto.getReservationId(), dto.getServiceId());

        return dto;
    }
}
