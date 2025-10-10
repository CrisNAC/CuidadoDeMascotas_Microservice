package com.cuidadodemascotas.microservice.apis.repositories;

import org.example.cuidadodemascota.commons.entities.reservation.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad ReservationService (relación muchos-a-muchos)
 */
@Repository
public interface ReservationServiceRepository extends JpaRepository<ReservationService, Long> {

    /**
     * Busca un ReservationService por ID que esté activo
     */
    @Query("SELECT rs FROM ReservationService rs WHERE rs.id = :id AND rs.active = true")
    Optional<ReservationService> findByIdAndActiveTrue(@Param("id") Long id);

    /**
     * Obtiene todos los ReservationServices activos con paginación
     */
    Page<ReservationService> findByActiveTrue(Pageable pageable);

    /**
     * Búsqueda con filtros: por reservationId o serviceId
     */
    @Query("""
            SELECT rs FROM ReservationService rs
            WHERE rs.active = true
            AND (:reservationId IS NULL OR rs.reservation.id = :reservationId)
            AND (:serviceId IS NULL OR rs.service.id = :serviceId)
            ORDER BY rs.createdAt DESC
            """)
    Page<ReservationService> findByFilters(
            @Param("reservationId") Long reservationId,
            @Param("serviceId") Long serviceId,
            Pageable pageable
    );

    /**
     * Obtiene todos los servicios de una reservación específica
     */
    @Query("SELECT rs FROM ReservationService rs WHERE rs.reservation.id = :reservationId AND rs.active = true")
    List<ReservationService> findByReservationIdAndActiveTrue(@Param("reservationId") Long reservationId);

    /**
     * Obtiene todas las reservaciones que tienen un servicio específico
     */
    @Query("SELECT rs FROM ReservationService rs WHERE rs.service.id = :serviceId AND rs.active = true")
    List<ReservationService> findByServiceIdAndActiveTrue(@Param("serviceId") Long serviceId);

    /**
     * Verifica si ya existe la relación entre una reservación y un servicio
     */
    @Query("""
            SELECT CASE WHEN COUNT(rs) > 0 THEN true ELSE false END
            FROM ReservationService rs
            WHERE rs.reservation.id = :reservationId
            AND rs.service.id = :serviceId
            AND rs.active = true
            """)
    boolean existsByReservationIdAndServiceIdAndActiveTrue(
            @Param("reservationId") Long reservationId,
            @Param("serviceId") Long serviceId
    );

    /**
     * Cuenta servicios de una reservación
     */
    @Query("SELECT COUNT(rs) FROM ReservationService rs WHERE rs.reservation.id = :reservationId AND rs.active = true")
    long countByReservationIdAndActiveTrue(@Param("reservationId") Long reservationId);
}
