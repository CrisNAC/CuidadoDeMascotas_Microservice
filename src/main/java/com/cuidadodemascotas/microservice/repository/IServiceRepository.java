package com.cuidadodemascotas.microservice.repository;

import org.example.cuidadodemascota.commons.entities.service.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para Service (para validaciones)
 */
@Repository
public interface IServiceRepository extends JpaRepository<Service, Long> {
        @Query("SELECT s FROM Service s WHERE s.id = :id AND s.active = true")
        Optional<Service> findByIdAndActiveTrue(@Param("id") Long id);
}
