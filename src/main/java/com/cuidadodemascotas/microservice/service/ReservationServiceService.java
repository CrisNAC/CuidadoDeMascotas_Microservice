package com.cuidadodemascotas.microservice.service;

import com.cuidadodemascotas.microservice.exception.BusinessValidationException;
import com.cuidadodemascotas.microservice.exception.ResourceConflictException;
import com.cuidadodemascotas.microservice.exception.ResourceNotFoundException;
import com.cuidadodemascotas.microservice.mapper.ReservationServiceMapper;
import com.cuidadodemascotas.microservice.repository.IReservationRepository;
import com.cuidadodemascotas.microservice.repository.IReservationServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cuidadodemascota.commons.dto.ReservationServiceRequestDTO;
import org.example.cuidadodemascota.commons.dto.ReservationServiceResponseDTO;
import org.example.cuidadodemascota.commons.entities.reservation.Reservation;
import org.example.cuidadodemascota.commons.entities.reservation.ReservationService;
import org.example.cuidadodemascota.commons.entities.service.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de ReservationServices (relación muchos-a-muchos)
 * Gestiona la asociación entre Reservations y Services
 */
@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ReservationServiceService {

    private final IReservationServiceRepository IReservationServiceRepository;
    private final IReservationRepository IReservationRepository;
    private final ReservationServiceMapper reservationServiceMapper;

    /**
     * Crea una nueva relación Reservation-Service
     * Valida que no exista duplicidad
     */
    @Transactional
    public ReservationServiceResponseDTO create(ReservationServiceRequestDTO requestDTO) {
        log.info("Creando ReservationService - ReservationId: {}, ServiceId: {}",
                requestDTO.getReservationId(), requestDTO.getServiceId());

        // Validaciones
        validateRequest(requestDTO);

        // Verificar que no exista ya la relación
        boolean exists = IReservationServiceRepository.existsByReservationIdAndServiceIdAndActiveTrue(
                Long.valueOf(requestDTO.getReservationId()), Long.valueOf(requestDTO.getServiceId()));

        if (exists) {
            log.error("Ya existe una relación activa entre Reservation {} y Service {}",
                    requestDTO.getReservationId(), requestDTO.getServiceId());
            throw new ResourceConflictException(
                    "Ya existe una relación activa entre esta reservación y servicio");
        }

        // Buscar Reservation
        Reservation reservation = IReservationRepository.findByIdAndActiveTrue(Long.valueOf(requestDTO.getReservationId()))
                .orElseThrow(() -> {
                    log.error("Reservation con ID {} no encontrada", requestDTO.getReservationId());
                    return new ResourceNotFoundException("Reservation", Long.valueOf(requestDTO.getReservationId()));
                });

        // Buscar Service
//        Service service = serviceRepository.findByIdAndActiveTrue(requestDTO.getServiceId())
//                .orElseThrow(() -> {
//                    log.error("Service con ID {} no encontrado", requestDTO.getServiceId());
//                    return new ResourceNotFoundException("Service", requestDTO.getServiceId());
//                });

        // Validar que el servicio pertenezca al carer de la reservación
        //validateServiceBelongsToCarer(service, reservation);

        // Crear entity
        ReservationService reservationService = reservationServiceMapper.toEntity(requestDTO);
        reservationService.setActive(true);

        // Guardar
        ReservationService saved = IReservationServiceRepository.save(reservationService);
        log.info("ReservationService creado exitosamente con ID: {}", saved.getId());

        return reservationServiceMapper.toResponseDTO(saved);
    }

    /**
     * Obtiene un ReservationService por ID
     */
    @Transactional(readOnly = true)
    public ReservationServiceResponseDTO findById(Long id) {
        log.info("Buscando ReservationService por ID: {}", id);

        ReservationService reservationService = IReservationServiceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    log.error("ReservationService con ID {} no encontrado", id);
                    return new ResourceNotFoundException("ReservationService", id);
                });

        log.debug("ReservationService encontrado: ID={}", reservationService.getId());
        return reservationServiceMapper.toResponseDTO(reservationService);
    }

    /**
     * Obtiene todos los ReservationServices activos con paginación
     */
    @Transactional(readOnly = true)
    public Page<ReservationServiceResponseDTO> findAll(Pageable pageable) {
        log.info("Obteniendo todos los ReservationServices - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<ReservationService> page = IReservationServiceRepository.findByActiveTrue(pageable);

        log.info("Se encontraron {} ReservationServices en la página {}",
                page.getNumberOfElements(), page.getNumber());

        return page.map(reservationServiceMapper::toResponseDTO);
    }

    /**
     * Busca ReservationServices con filtros y paginación
     */
    @Transactional(readOnly = true)
    public Page<ReservationServiceResponseDTO> findByFilters(
            Long reservationId, Long serviceId, Pageable pageable) {

        log.info("Buscando ReservationServices con filtros - ReservationId: {}, ServiceId: {}",
                reservationId, serviceId);

        Page<ReservationService> page = IReservationServiceRepository.findByFilters(
                reservationId, serviceId, pageable);

        log.info("Se encontraron {} ReservationServices con los filtros aplicados",
                page.getTotalElements());

        return page.map(reservationServiceMapper::toResponseDTO);
    }

    /**
     * Obtiene todos los servicios de una reservación específica
     */
    @Transactional(readOnly = true)
    public List<ReservationServiceResponseDTO> findByReservationId(Long reservationId) {
        log.info("Obteniendo servicios de la Reservation ID: {}", reservationId);

        // Verificar que la reservación exista
        IReservationRepository.findByIdAndActiveTrue(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        List<ReservationService> services = IReservationServiceRepository
                .findByReservationIdAndActiveTrue(reservationId);

        log.info("Se encontraron {} servicios para la Reservation ID: {}",
                services.size(), reservationId);

        return services.stream()
                .map(reservationServiceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las reservaciones que tienen un servicio específico
     */
    @Transactional(readOnly = true)
    public List<ReservationServiceResponseDTO> findByServiceId(Long serviceId) {
        log.info("Obteniendo reservaciones del Service ID: {}", serviceId);

        // Verificar que el servicio exista
//        serviceRepository.findByIdAndActiveTrue(serviceId)
//                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        List<ReservationService> reservations = IReservationServiceRepository
                .findByServiceIdAndActiveTrue(serviceId);

        log.info("Se encontraron {} reservaciones para el Service ID: {}",
                reservations.size(), serviceId);

        return reservations.stream()
                .map(reservationServiceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Borrado lógico de una relación Reservation-Service
     */
    @Transactional
    public void delete(Long id) {
        log.info("Eliminando (borrado lógico) ReservationService ID: {}", id);

        ReservationService reservationService = IReservationServiceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    log.error("ReservationService con ID {} no encontrado para eliminar", id);
                    return new ResourceNotFoundException("ReservationService", id);
                });

        // Borrado lógico
        reservationService.setActive(false);
        IReservationServiceRepository.save(reservationService);

        log.info("ReservationService ID: {} eliminado (borrado lógico) exitosamente", id);
    }

    /**
     * Elimina todos los servicios de una reservación
     */
    @Transactional
    public void deleteAllByReservationId(Long reservationId) {
        log.info("Eliminando todos los servicios de la Reservation ID: {}", reservationId);

        List<ReservationService> services = IReservationServiceRepository
                .findByReservationIdAndActiveTrue(reservationId);

        services.forEach(rs -> {
            rs.setActive(false);
            IReservationServiceRepository.save(rs);
        });

        log.info("{} servicios eliminados de la Reservation ID: {}", services.size(), reservationId);
    }

    // ========== MÉTODOS DE VALIDACIÓN ==========

    private void validateRequest(ReservationServiceRequestDTO dto) {
        log.debug("Validando ReservationServiceRequestDTO");

        if (dto.getReservationId() == null) {
            throw new BusinessValidationException("El ID de la Reservation es obligatorio");
        }
        if (dto.getServiceId() == null) {
            throw new BusinessValidationException("El ID del Service es obligatorio");
        }

        log.debug("Validaciones pasadas correctamente");
    }

    private void validateServiceBelongsToCarer(Service service, Reservation reservation) {
        log.debug("Validando que el Service pertenezca al Carer de la Reservation");

        if (!service.getCarer().getId().equals(reservation.getCarer().getId())) {
            log.error("El Service ID: {} no pertenece al Carer ID: {} de la Reservation",
                    service.getId(), reservation.getCarer().getId());
            throw new BusinessValidationException(
                    "El servicio no pertenece al cuidador asignado a esta reservación");
        }

        log.debug("El servicio pertenece al carer correcto");
    }
}