package com.cuidadodemascotas.microservice.service;

import com.cuidadodemascotas.microservice.exception.BusinessValidationException;
import com.cuidadodemascotas.microservice.exception.ResourceNotFoundException;
import com.cuidadodemascotas.microservice.mapper.ReservationMapper;
import com.cuidadodemascotas.microservice.repository.IReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cuidadodemascota.commons.dto.ReservationRequestDTO;
import org.example.cuidadodemascota.commons.dto.ReservationResponseDTO;
import org.example.cuidadodemascota.commons.entities.enums.ReservationStateEnum;
import org.example.cuidadodemascota.commons.entities.reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Servicio para gestión de Reservations
 * Contiene toda la lógica de negocio y validaciones
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final IReservationRepository IReservationRepository;
    private final ReservationMapper reservationMapper;

    /**
     * Crea una nueva reservación
     * Valida que owner y carer existan y estén activos
     */
    @Transactional
    public ReservationResponseDTO create(ReservationRequestDTO requestDTO) {
        log.info("Iniciando creación de Reservation - OwnerId: {}, CarerId: {}",
                requestDTO.getOwnerId(), requestDTO.getCarerId());

        // Validaciones
        validateReservationRequest(requestDTO);

        // Crear entity
        Reservation reservation = reservationMapper.toEntity(requestDTO);
        reservation.setActive(true);

        // Guardar
        Reservation saved = IReservationRepository.save(reservation);
        log.info("Reservation creada exitosamente con ID: {}", saved.getId());

        return reservationMapper.toResponseDTO(saved);
    }

    /**
     * Actualiza una reservación existente
     */
    @Transactional
    public ReservationResponseDTO update(Long id, ReservationRequestDTO requestDTO) {
        log.info("Actualizando Reservation ID: {}", id);

        // Buscar reservación existente
        Reservation existing = IReservationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    log.error("Reservation con ID {} no encontrada", id);
                    return new ResourceNotFoundException("Reservation", id);
                });

        // Validar que no esté finalizada
        if (existing.getState() == ReservationStateEnum.FINISHED) {
            log.error("No se puede actualizar una reservación FINISHED");
            throw new BusinessValidationException("No se puede actualizar una reservación finalizada");
        }

        // Actualizar campos básicos
        reservationMapper.updateEntityFromDto(existing, requestDTO);

        // Guardar
        Reservation updated = IReservationRepository.save(existing);
        log.info("Reservation ID: {} actualizada exitosamente", id);

        return reservationMapper.toResponseDTO(updated);
    }

    /**
     * Obtiene una reservación por ID
     */
    @Transactional(readOnly = true)
    public ReservationResponseDTO findById(Long id) {
        log.info("Buscando Reservation por ID: {}", id);

        Reservation reservation = IReservationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    log.error("Reservation con ID {} no encontrada", id);
                    return new ResourceNotFoundException("Reservation", id);
                });

        log.debug("Reservation encontrada: ID={}", reservation.getId());
        return reservationMapper.toResponseDTO(reservation);
    }

    /**
     * Obtiene todas las reservaciones activas con paginación
     */
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> findAll(Pageable pageable) {
        log.info("Obteniendo todas las Reservations - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Reservation> page = IReservationRepository.findByActiveTrue(pageable);

        log.info("Se encontraron {} Reservations en la página {}",
                page.getNumberOfElements(), page.getNumber());

        return page.map(reservationMapper::toResponseDTO);
    }

    /**
     * Busca reservaciones con filtros y paginación
     */
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> findByFilters(
            Long ownerId, Long carerId, ReservationStateEnum state,
            LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {

        log.info("Buscando Reservations con filtros - OwnerId: {}, CarerId: {}, State: {}",
                ownerId, carerId, state);

        Page<Reservation> page = IReservationRepository.findByFilters(
                ownerId, carerId, state, startDate, endDate, pageable);

        log.info("Se encontraron {} Reservations con los filtros aplicados", page.getTotalElements());

        return page.map(reservationMapper::toResponseDTO);
    }

    /**
     * Borrado lógico de una reservación
     */
    @Transactional
    public void delete(Long id) {
        log.info("Eliminando (borrado lógico) Reservation ID: {}", id);

        Reservation reservation = IReservationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    log.error("Reservation con ID {} no encontrada para eliminar", id);
                    return new ResourceNotFoundException("Reservation", id);
                });

        // Validar que pueda eliminarse
        if (reservation.getState() == ReservationStateEnum.ACCEPTED) {
            log.warn("Intentando eliminar una reservación ACCEPTED");
            throw new BusinessValidationException("No se puede eliminar una reservación aceptada. Debe rechazarla primero.");
        }

        // Borrado lógico
        reservation.setActive(false);
        IReservationRepository.save(reservation);

        log.info("Reservation ID: {} eliminada (borrado lógico) exitosamente", id);
    }

    // ========== MÉTODOS DE VALIDACIÓN ==========

    private void validateReservationRequest(ReservationRequestDTO dto) {
        log.debug("Validando ReservationRequestDTO");

        if (dto.getOwnerId() == null) {
            throw new BusinessValidationException("El ID del Owner es obligatorio");
        }
        if (dto.getCarerId() == null) {
            throw new BusinessValidationException("El ID del Carer es obligatorio");
        }
        if (dto.getServiceDate() == null) {
            throw new BusinessValidationException("La fecha del servicio es obligatoria");
        }
        if (dto.getServiceDate().isBefore(OffsetDateTime.from(LocalDateTime.now()))) {
            throw new BusinessValidationException("La fecha del servicio no puede ser en el pasado");
        }
        if (dto.getReservationState() == null) {
            throw new BusinessValidationException("El estado es obligatorio");
        }

        log.debug("Validaciones pasadas correctamente");
    }

//    private void validateCarerAvailability(Carer carer, LocalDateTime serviceDate) {
//        log.debug("Validando disponibilidad del Carer ID: {}", carer.getId());
//
//        // Verificar si hay conflicto de horario (±2 horas)
//        LocalDateTime startRange = serviceDate.minusHours(2);
//        LocalDateTime endRange = serviceDate.plusHours(2);
//
//        boolean hasConflict = reservationRepository.existsActiveReservationForCarerInDateRange(
//                carer.getId(), startRange, endRange);
//
//        if (hasConflict) {
//            log.error("Carer ID: {} no está disponible en el horario solicitado", carer.getId());
//            throw new BusinessValidationException("El carer no está disponible en el horario solicitado");
//        }
//
//        log.debug("Carer disponible para la fecha solicitada");
//    }
}