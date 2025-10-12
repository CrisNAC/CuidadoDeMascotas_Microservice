package com.cuidadodemascotas.microservice.repository;

import org.example.cuidadodemascota.commons.entities.user.Carer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para Carer (para validaciones)
 */
@Repository
public interface ICarerRepository extends JpaRepository<Carer, Long> {
    @Query("SELECT c FROM Carer c WHERE c.id = :id AND c.active = true")
    Optional<Carer> findByIdAndActiveTrue(@Param("id") Long id);
}
