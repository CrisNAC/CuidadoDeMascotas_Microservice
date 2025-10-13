package com.cuidadodemascotas.microservice.service.reservation;

import org.example.cuidadodemascota.commons.dto.ReservationRequestDTO;
import com.cuidadodemascotas.microservice.service.base.IBaseService;
import org.example.cuidadodemascota.commons.dto.ReservationResponseDTO;
import org.example.cuidadodemascota.commons.dto.ReservationResult;
import org.example.cuidadodemascota.commons.entities.enums.ReservationStateEnum;
import org.example.cuidadodemascota.commons.entities.user.Carer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface IReservationService extends IBaseService<ReservationRequestDTO, ReservationResponseDTO, ReservationResult> {

    //ReservationResponseDTO create(ReservationRequestDTO requestDTO);

    ReservationResponseDTO update(Long id, ReservationRequestDTO requestDTO);

    //ReservationResponseDTO findById(Long id);

    Page<ReservationResponseDTO> findAll(Pageable pageable);

    Page<ReservationResponseDTO> findByFilters(
            Long ownerId, Long carerId, ReservationStateEnum state,
            LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable);

    void delete(Long id);
}
