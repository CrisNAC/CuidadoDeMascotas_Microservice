package com.cuidadodemascotas.microservice.repository;

import org.example.cuidadodemascota.commons.entities.enums.ReservationStateEnum;
import org.example.cuidadodemascota.commons.entities.reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio para la entidad Reservation
 * Incluye queries personalizadas para búsquedas con filtros
 */
@Repository
public interface IReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Busca una reservación por ID que esté activa
     */
    @Query("SELECT r FROM Reservation r WHERE r.id = :id AND r.active = true")
    Optional<Reservation> findByIdAndActiveTrue(@Param("id") Long id);

    /**
     * Obtiene todas las reservaciones activas con paginación
     */
    Page<Reservation> findByActiveTrue(Pageable pageable);

    /**
     * Búsqueda con filtros opcionales
     * Permite buscar por: ownerId, carerId, estado, rango de fechas
     */
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.active = true
            AND (:ownerId IS NULL OR r.owner.id = :ownerId)
            AND (:carerId IS NULL OR r.carer.id = :carerId)
            AND (:state IS NULL OR r.state = :state)
            AND (CAST(:startDate AS timestamp) IS NULL OR r.serviceDate >= :startDate)
            AND (CAST(:endDate AS timestamp) IS NULL OR r.serviceDate <= :endDate)
            ORDER BY r.serviceDate DESC
            """)
    Page<Reservation> findByFilters(
            @Param("ownerId") Long ownerId,
            @Param("carerId") Long carerId,
            @Param("state") ReservationStateEnum state,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Cuenta reservaciones activas de un owner
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.owner.id = :ownerId AND r.active = true")
    long countByOwnerIdAndActiveTrue(@Param("ownerId") Long ownerId);

    /**
     * Cuenta reservaciones activas de un carer
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.carer.id = :carerId AND r.active = true")
    long countByCarerIdAndActiveTrue(@Param("carerId") Long carerId);

    /**
     * Verifica si existe una reservación en un rango de fechas para un carer
     */
    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Reservation r
            WHERE r.carer.id = :carerId
            AND r.active = true
            AND r.state IN ('PENDING', 'ACCEPTED')
            AND r.serviceDate BETWEEN :startDate AND :endDate
            """)
    boolean existsActiveReservationForCarerInDateRange(
            @Param("carerId") Long carerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}