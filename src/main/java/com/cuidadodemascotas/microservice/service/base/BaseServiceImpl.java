package com.cuidadodemascotas.microservice.service.base;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @param <DI> Request DTO (DTO Input)
 * @param <DO> Response DTO (DTO Output)
 * @param <E> Entity
 * @param <R> Response Wrapper (e.g., BaseResult<DO>)
 */
@Slf4j
public abstract class BaseServiceImpl<DI, DO, E, R>
        implements IBaseService<DI, DO, R> {

    protected abstract DO convertEntityToDto(E entity);

    protected abstract E convertDtoToEntity(DI dto);

    protected List<DO> covertEntityListToDto(List<E> entityList) {
        log.info("Convirtiendo lista de entities a DTOs: {}", entityList);
        List<DO> dtoList = entityList.stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
        log.info("Lista de entities convertida a DTOs: {}", dtoList);
        return dtoList;
    }

    protected List<E> covertDtoListToEntity(List<DI> dtoList) {
        log.info("Convirtiendo lista de DTOs a entities: {}", dtoList);
        List<E> entityList = dtoList.stream()
                .map(this::convertDtoToEntity)
                .collect(Collectors.toList());
        log.info("Lista de DTOs convertida a entities: {}", entityList);
        return entityList;
    }
}
