package com.cuidadodemascotas.microservice.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s con ID %d no encontrado", resourceName, id));
    }
}