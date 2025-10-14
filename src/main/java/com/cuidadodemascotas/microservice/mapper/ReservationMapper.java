package com.cuidadodemascotas.microservice.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cuidadodemascota.commons.dto.ReservationRequestDTO;
import org.example.cuidadodemascota.commons.dto.ReservationResponseDTO;
import org.example.cuidadodemascota.commons.entities.enums.ReservationStateEnum;
import org.example.cuidadodemascota.commons.entities.reservation.Reservation;
import org.example.cuidadodemascota.commons.entities.user.Carer;
import org.example.cuidadodemascota.commons.entities.user.Owner;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
public class ReservationMapper implements IBaseMapper<Reservation, ReservationRequestDTO, ReservationResponseDTO> {

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

        // Convertir enum de DTO a Entity
        if (dto.getReservationState() != null) {
            entity.setState(convertToEntityEnum(dto.getReservationState()));
        }

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
            entity.setState(convertToEntityEnum(dto.getReservationState()));
        }

        log.debug("Entity actualizada: serviceDate={}, state={}",
                entity.getServiceDate(), entity.getState());
    }

    /**
     * Convierte Entity a Response DTO
     * IMPORTANTE: Solo mapea IDs de las relaciones para evitar lazy loading
     */
    public ReservationResponseDTO toDto(Reservation entity) {
        log.debug("Convirtiendo Reservation Entity a ResponseDTO");

        if (entity == null) {
            log.warn("Entity recibida es null");
            return null;
        }

        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(entity.getId());

        //dto.setServiceDate(OffsetDateTime.from(entity.getServiceDate()));
        dto.setServiceDate(entity.getServiceDate() != null
                ? entity.getServiceDate().atOffset(ZoneOffset.of("-03:00"))
                : null);

        // Convertir enum de Entity a DTO
        if (entity.getState() != null) {
            dto.setReservationState(convertToDtoEnum(entity.getState()));
        }

//        dto.setCreatedAt(OffsetDateTime.from(entity.getCreatedAt()));
//        dto.setUpdatedAt(OffsetDateTime.from(entity.getUpdatedAt()));
        dto.setCreatedAt(entity.getCreatedAt() != null
                ? entity.getCreatedAt().atOffset(ZoneOffset.of("-03:00"))
                : null);

        dto.setUpdatedAt(entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().atOffset(ZoneOffset.of("-03:00"))
                : null);

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

    /**
     * Configura las relaciones de la entity (Owner y Carer)
     * Se llama desde el servicio después de buscar las entidades relacionadas
     */
    public void setRelations(Reservation entity, Owner owner, Carer carer) {
        log.debug("Configurando relaciones para Reservation");

        if (owner != null) {
            entity.setOwner(owner);
            log.debug("Owner configurado: ID={}", owner.getId());
        }
        if (carer != null) {
            entity.setCarer(carer);
            log.debug("Carer configurado: ID={}", carer.getId());
        }
    }

    // ========== MÉTODOS DE CONVERSIÓN DE ENUMS ==========

    /**
     * Convierte el enum del DTO al enum de la Entity
     */
    private ReservationStateEnum convertToEntityEnum(ReservationRequestDTO.ReservationStateEnum dtoEnum) {
        if (dtoEnum == null) {
            return null;
        }

        // Convertir por nombre (ambos deben tener los mismos valores)
        return ReservationStateEnum.valueOf(dtoEnum.name());
    }

    /**
     * Convierte el enum de la Entity al enum del DTO
     */
    private ReservationResponseDTO.ReservationStateEnum convertToDtoEnum(ReservationStateEnum entityEnum) {
        if (entityEnum == null) {
            return null;
        }

        // Convertir por nombre (ambos deben tener los mismos valores)
        return ReservationResponseDTO.ReservationStateEnum.valueOf(entityEnum.name());
    }

}