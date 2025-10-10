package com.cuidadodemascotas.microservice.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cuidadodemascota.commons.dto.ReservationRequestDTO;
import org.example.cuidadodemascota.commons.dto.ReservationResponseDTO;
import org.example.cuidadodemascota.commons.entities.reservation.Reservation;
import org.example.cuidadodemascota.commons.entities.user.Carer;
import org.example.cuidadodemascota.commons.entities.user.Owner;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Mapper manual para conversión entre Reservation Entity y DTOs
 * Se usa mapper manual en lugar de ModelMapper para evitar problemas con:
 * - Entidades LAZY
 * - Relaciones bidireccionales
 * - Control total sobre la conversión
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationMapper {

    /**
     * Convierte Request DTO a Entity (para creación)
     * Solo mapea los campos básicos, las relaciones se setean en el servicio
     */
    public Reservation toEntity(ReservationRequestDTO dto) {
        log.debug("Convirtiendo ReservationRequestDTO a Entity");

        if (dto == null) {
            log.warn("DTO recibido es null");
            return null;
        }

        Reservation entity = new Reservation();
        entity.setServiceDate(dto.getServiceDate().toLocalDateTime());
        entity.setState(dto.getReservationState());

        log.debug("Entity creada: serviceDate={}, state={}",
                entity.getServiceDate(), entity.getState());

        return entity;
    }

    /**
     * Actualiza una entity existente con los datos del Request DTO
     * IMPORTANTE: No actualiza las relaciones (owner, carer), solo datos básicos
     */
    public void updateEntityFromDto(Reservation entity, ReservationRequestDTO dto) {
        log.debug("Actualizando Reservation entity ID={} desde DTO", entity.getId());

        if (dto.getServiceDate() != null) {
            entity.setServiceDate(dto.getServiceDate().toLocalDateTime());
        }
        if (dto.getReservationState() != null) {
            entity.setState(dto.getReservationState());
        }

        log.debug("Entity actualizada: serviceDate={}, state={}",
                entity.getServiceDate(), entity.getState());
    }

    /**
     * Convierte Entity a Response DTO
     * IMPORTANTE: Solo mapea IDs de las relaciones para evitar lazy loading
     */
    public ReservationResponseDTO toResponseDTO(Reservation entity) {
        log.debug("Convirtiendo Reservation Entity a ResponseDTO");

        if (entity == null) {
            log.warn("Entity recibida es null");
            return null;
        }

        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(entity.getId());
        dto.setServiceDate(OffsetDateTime.from(entity.getServiceDate()));
        dto.setReservationState(entity.getState());
        dto.setCreatedAt(OffsetDateTime.from(entity.getCreatedAt()));
        dto.setUpdatedAt(OffsetDateTime.from(entity.getUpdatedAt()));
        dto.setActive(entity.getActive());

        // Mapeo de relaciones - Solo IDs para evitar lazy loading exceptions
        if (entity.getOwner() != null) {
            dto.setOwnerId(Math.toIntExact(entity.getOwner().getId()));
        }
        if (entity.getCarer() != null) {
            dto.setCarerId(Math.toIntExact(entity.getCarer().getId()));
        }

        log.debug("ResponseDTO creado: id={}, ownerId={}, carerId={}",
                dto.getId(), dto.getOwnerId(), dto.getCarerId());

        return dto;
    }
}