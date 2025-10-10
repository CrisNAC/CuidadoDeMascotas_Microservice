package com.cuidadodemascotas.microservice.exception;

/**
 * Excepción lanzada cuando hay un conflicto con el estado actual del recurso
 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}