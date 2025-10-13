package com.cuidadodemascotas.microservice.service.reservationservice;

import com.cuidadodemascotas.microservice.service.base.IBaseService;
import org.example.cuidadodemascota.commons.dto.ReservationServiceRequestDTO;
import org.example.cuidadodemascota.commons.dto.ReservationServiceResponseDTO;
import org.example.cuidadodemascota.commons.dto.ReservationServiceResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IReservationServiceService extends IBaseService<ReservationServiceRequestDTO, ReservationServiceResponseDTO, ReservationServiceResult> {

    Page<ReservationServiceResponseDTO> findAll(Pageable pageable);

    Page<ReservationServiceResponseDTO> findByFilters(Long reservationId, Long serviceId, Pageable pageable);

    List<ReservationServiceResponseDTO> findByReservationId(Long reservationId);

    List<ReservationServiceResponseDTO> findByServiceId(Long serviceId);

    void deleteAllByReservationId(Long reservationId);
}
