package com.cuidadodemascotas.microservice.repository;

import org.example.cuidadodemascota.commons.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para User (para validaciones)
 */
@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.active = true")
    Optional<User> findByIdAndActiveTrue(@Param("id") Long id);
}
