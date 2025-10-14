package com.cuidadodemascotas.microservice.service.reservation;

import com.cuidadodemascotas.microservice.exception.BusinessValidationException;
import com.cuidadodemascotas.microservice.exception.ResourceNotFoundException;
import com.cuidadodemascotas.microservice.mapper.ReservationMapper;
import com.cuidadodemascotas.microservice.repository.ICarerRepository;
import com.cuidadodemascotas.microservice.repository.IOwnerRepository;
import com.cuidadodemascotas.microservice.repository.IReservationRepository;
import com.cuidadodemascotas.microservice.service.base.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cuidadodemascota.commons.dto.ReservationRequestDTO;
import org.example.cuidadodemascota.commons.dto.ReservationResponseDTO;
import org.example.cuidadodemascota.commons.dto.ReservationResult;
import org.example.cuidadodemascota.commons.entities.enums.ReservationStateEnum;
import org.example.cuidadodemascota.commons.entities.reservation.Reservation;
import org.example.cuidadodemascota.commons.entities.user.Carer;
import org.example.cuidadodemascota.commons.entities.user.Owner;
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
public class ReservationServiceImpl
        extends BaseServiceImpl<ReservationRequestDTO, ReservationResponseDTO, Reservation, ReservationResult>
        implements IReservationService {

    private final IReservationRepository reservationRepository;
    private final IOwnerRepository ownerRepository;
    private final ICarerRepository carerRepository;
    private final ReservationMapper reservationMapper;

    protected ReservationResponseDTO convertEntityToDto(Reservation entity) {
        return reservationMapper.toDto(entity);
    }

    protected Reservation convertDtoToEntity(ReservationRequestDTO dto) {
        return reservationMapper.toEntity(dto);
    }

    /**
     * Crea una nueva reservación
     * Valida que owner y carer existan y estén activos
     */
    @Transactional
    public ReservationResponseDTO save(ReservationRequestDTO requestDTO) {
        log.info("Iniciando creación de Reservation - OwnerId: {}, CarerId: {}",
                requestDTO.getOwnerId(), requestDTO.getCarerId());

        // Validaciones
        validateReservationRequest(requestDTO);

        // Buscar Owner y Carer
        Owner owner = ownerRepository.findByIdAndActiveTrue(Long.valueOf(requestDTO.getOwnerId()))
                .orElseThrow(() -> {
                    log.error("Owner con ID {} no encontrado o inactivo", requestDTO.getOwnerId());
                    return new ResourceNotFoundException("Owner", Long.valueOf(requestDTO.getOwnerId()));
                });
        log.debug("Owner encontrado: ID={}", owner.getId());

        Carer carer = carerRepository.findByIdAndActiveTrue(Long.valueOf(requestDTO.getCarerId()))
                .orElseThrow(() -> {
                    log.error("Carer con ID {} no encontrado o inactivo", requestDTO.getCarerId());
                    return new ResourceNotFoundException("Carer", Long.valueOf(requestDTO.getCarerId()));
                });
        log.debug("Carer encontrado: ID={}", carer.getId());

        // Validar disponibilidad del carer
        validateCarerAvailability(carer, requestDTO.getServiceDate().toLocalDateTime());

        // Crear entity
        Reservation reservation = reservationMapper.toEntity(requestDTO);
        reservationMapper.setRelations(reservation, owner, carer);
        reservation.setActive(true);

        // Guardar
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation creada exitosamente con ID: {}", saved.getId());

        return reservationMapper.toDto(saved);
    }

    /**
     * Actualiza una reservación existente
     */
    @Transactional
    public ReservationResponseDTO update(Long id, ReservationRequestDTO requestDTO) {
        log.info("Actualizando Reservation ID: {}", id);

        // Buscar reservación existente
        Reservation existing = reservationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    log.error("Reservation con ID {} no encontrada", id);
                    return new ResourceNotFoundException("Reservation", id);
                });

        // Validar que no esté finalizada
        if (existing.getState() == ReservationStateEnum.FINISHED) {
            log.error("No se puede actualizar una reservación FINISHED");
            throw new BusinessValidationException("No se puede actualizar una reservación finalizada");
        }

        // Actualizar relaciones si cambiaron
        if (requestDTO.getOwnerId() != null && !requestDTO.getOwnerId().equals(existing.getOwner().getId())) {
            Owner newOwner = ownerRepository.findByIdAndActiveTrue(Long.valueOf(requestDTO.getOwnerId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Owner", Long.valueOf(requestDTO.getOwnerId())));
            existing.setOwner(newOwner);
            log.debug("Owner actualizado a ID: {}", newOwner.getId());
        }

        if (requestDTO.getCarerId() != null && !requestDTO.getCarerId().equals(existing.getCarer().getId())) {
            Carer newCarer = carerRepository.findByIdAndActiveTrue(Long.valueOf(requestDTO.getCarerId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Carer", Long.valueOf(requestDTO.getCarerId())));
            validateCarerAvailability(newCarer, requestDTO.getServiceDate().toLocalDateTime());
            existing.setCarer(newCarer);
            log.debug("Carer actualizado a ID: {}", newCarer.getId());
        }

        // Actualizar campos básicos
        reservationMapper.updateEntityFromDto(existing, requestDTO);

        // Guardar
        Reservation updated = reservationRepository.save(existing);
        log.info("Reservation ID: {} actualizada exitosamente", id);

        return reservationMapper.toDto(updated);
    }

    /**
     * Obtiene una reservación por ID
     */
    @Transactional(readOnly = true)
    public ReservationResponseDTO getById(Long id) {
        log.info("Buscando Reservation por ID: {}", id);

        Reservation reservation = reservationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    log.error("Reservation con ID {} no encontrada", id);
                    return new ResourceNotFoundException("Reservation", id);
                });

        log.debug("Reservation encontrada: ID={}", reservation.getId());
        return reservationMapper.toDto(reservation);
    }

    /**
     * Obtiene todas las reservaciones activas con paginación
     */
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> findAll(Pageable pageable) {
        log.info("Obteniendo todas las Reservations - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Reservation> page = reservationRepository.findByActiveTrue(pageable);

        log.info("Se encontraron {} Reservations en la página {}",
                page.getNumberOfElements(), page.getNumber());

        return page.map(reservationMapper::toDto);
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

        Page<Reservation> page = reservationRepository.findByFilters(
                ownerId, carerId, state, startDate, endDate, pageable);

        log.info("Se encontraron {} Reservations con los filtros aplicados", page.getTotalElements());

        return page.map(reservationMapper::toDto);
    }

    /**
     * Borrado lógico de una reservación
     */
    @Transactional
    public void delete(Long id) {
        log.info("Eliminando (borrado lógico) Reservation ID: {}", id);

        Reservation reservation = reservationRepository.findByIdAndActiveTrue(id)
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
        reservationRepository.save(reservation);

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
        if (dto.getServiceDate().isBefore(OffsetDateTime.now())) {
            throw new BusinessValidationException("La fecha del servicio no puede ser en el pasado");
        }
        if (dto.getReservationState() == null) {
            throw new BusinessValidationException("El estado es obligatorio");
        }

        log.debug("Validaciones pasadas correctamente");
    }

    private void validateCarerAvailability(Carer carer, LocalDateTime serviceDate) {
        log.debug("Validando disponibilidad del Carer ID: {}", carer.getId());

        // Verificar si hay conflicto de horario (±2 horas)
        LocalDateTime startRange = serviceDate.minusHours(2);
        LocalDateTime endRange = serviceDate.plusHours(2);

        boolean hasConflict = reservationRepository.existsActiveReservationForCarerInDateRange(
                carer.getId(), startRange, endRange);

        if (hasConflict) {
            log.error("Carer ID: {} no está disponible en el horario solicitado", carer.getId());
            throw new BusinessValidationException("El carer no está disponible en el horario solicitado");
        }

        log.debug("Carer disponible para la fecha solicitada");
    }
}