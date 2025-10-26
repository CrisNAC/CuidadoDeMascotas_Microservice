package com.cuidadodemascotas.microservice.service.base;

/**
 *
 * @param <DI> Request DTO (DTO Input)
 * @param <DO> Response DTO (DTO Output)
 * @param <R> Response Wrapper (e.g., BaseResult<DO>)
 */
public interface IBaseService<DI, DO, R> {
   DO save(DI dto);

   DO getById(Long id);

}
