package com.cuidadodemascotas.microservice.repository;

import org.example.cuidadodemascota.commons.entities.user.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para Owner (para validaciones)
 */
@Repository
public interface IOwnerRepository extends JpaRepository<Owner, Long> {
    @Query("SELECT o FROM Owner o WHERE o.id = :id AND o.active = true")
    Optional<Owner> findByIdAndActiveTrue(@Param("id") Long id);
}
