package com.cuidadodemascotas.microservice.exception;

/**
 * Excepción lanzada cuando hay un error de validación de negocio
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}