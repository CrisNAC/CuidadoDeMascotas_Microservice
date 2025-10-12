package com.cuidadodemascotas.microservice.mapper;

/**
 *
 * @param <E> Entity
 * @param <DI> Request DTO (DTO Input)
 * @param <DO> Response DTO (DTO Output)
 */
public interface IBaseMapper<E, DI, DO> {
    E toEntity(DI dto);
    DO toDto(E entity);
}
