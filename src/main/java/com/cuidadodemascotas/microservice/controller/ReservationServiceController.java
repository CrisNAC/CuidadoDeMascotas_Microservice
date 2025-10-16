package com.cuidadodemascotas.microservice.controller;

import com.cuidadodemascotas.microservice.service.reservationservice.ReservationServiceServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cuidadodemascota.commons.dto.ReservationServiceRequestDTO;
import org.example.cuidadodemascota.commons.dto.ReservationServiceResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reservation-services")
@RequiredArgsConstructor
@Tag(name = "Reservation Services", description = "API para gestionar la relación entre Reservaciones y Servicios")
public class ReservationServiceController {

    private final ReservationServiceServiceImpl reservationServiceServiceImpl;

    // ===== CREATE =====
    @Operation(summary = "Crear una nueva relación Reservation-Service",
            description = "Crea un nuevo vínculo entre una reservación y un servicio, validando duplicados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Relación creada exitosamente",
                    content = @Content(schema = @Schema(implementation = ReservationServiceResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Reservación o servicio no encontrado"),
            @ApiResponse(responseCode = "409", description = "Relación ya existente")
    })
    @PostMapping
    public ResponseEntity<ReservationServiceResponseDTO> create(
            @RequestBody ReservationServiceRequestDTO requestDTO) {

        log.info("POST /reservation-services - Crear relación Reservation-Service");
        log.debug("Request body: {}", requestDTO);

        ReservationServiceResponseDTO response = reservationServiceServiceImpl.save(requestDTO);

        log.info("Relación creada con ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ===== GET BY ID =====
    @Operation(summary = "Obtener relación por ID",
            description = "Obtiene una relación Reservation-Service por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relación encontrada",
                    content = @Content(schema = @Schema(implementation = ReservationServiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Relación no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservationServiceResponseDTO> findById(
            @Parameter(description = "ID de la relación Reservation-Service") @PathVariable Long id) {

        log.info("GET /reservation-services/{} - Buscar por ID", id);

        ReservationServiceResponseDTO response = reservationServiceServiceImpl.getById(id);

        log.info("Relación encontrada con ID: {}", id);
        return ResponseEntity.ok(response);
    }

    // ===== FIND ALL =====
    @Operation(summary = "Listar todas las relaciones",
            description = "Obtiene todas las relaciones Reservation-Service activas con paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<ReservationServiceResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("GET /reservation-services - Listar todas (page={}, size={}, sortBy={}, sortDir={})",
                page, size, sortBy, sortDir);

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ReservationServiceResponseDTO> response = reservationServiceServiceImpl.findAll(pageable);

        log.info("Se obtuvieron {} relaciones de {} totales",
                response.getNumberOfElements(), response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    // ===== SEARCH =====
    @Operation(summary = "Buscar relaciones con filtros",
            description = "Busca relaciones Reservation-Service aplicando filtros opcionales con paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ReservationServiceResponseDTO>> search(
            @Parameter(description = "ID de la reservación") @RequestParam(required = false) Long reservationId,
            @Parameter(description = "ID del servicio") @RequestParam(required = false) Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("GET /reservation-services/search - Filtros: reservationId={}, serviceId={}",
                reservationId, serviceId);

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ReservationServiceResponseDTO> response =
                reservationServiceServiceImpl.findByFilters(reservationId, serviceId, pageable);

        log.info("Búsqueda completada: {} resultados encontrados", response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    // ===== FIND BY RESERVATION =====
    @Operation(summary = "Obtener servicios de una reservación",
            description = "Obtiene todos los servicios asociados a una reservación específica")
    @GetMapping("/by-reservation/{reservationId}")
    public ResponseEntity<List<ReservationServiceResponseDTO>> findByReservationId(
            @PathVariable Long reservationId) {

        log.info("GET /reservation-services/by-reservation/{} - Buscar servicios", reservationId);

        List<ReservationServiceResponseDTO> response =
                reservationServiceServiceImpl.findByReservationId(reservationId);

        return ResponseEntity.ok(response);
    }

    // ===== FIND BY SERVICE =====
    @Operation(summary = "Obtener reservaciones de un servicio",
            description = "Obtiene todas las reservaciones que utilizan un servicio específico")
    @GetMapping("/by-service/{serviceId}")
    public ResponseEntity<List<ReservationServiceResponseDTO>> findByServiceId(
            @PathVariable Long serviceId) {

        log.info("GET /reservation-services/by-service/{} - Buscar reservaciones", serviceId);

        List<ReservationServiceResponseDTO> response =
                reservationServiceServiceImpl.findByServiceId(serviceId);

        return ResponseEntity.ok(response);
    }

    // ===== UPDATE (SIMPLE) =====
    @Operation(summary = "Actualizar relación Reservation-Service",
            description = "Modifica una relación existente entre una reservación y un servicio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relación actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = ReservationServiceResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Relación no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReservationServiceResponseDTO> update(
            @Parameter(description = "ID de la relación Reservation-Service") @PathVariable Long id,
            @RequestBody ReservationServiceRequestDTO requestDTO) {

        log.info("PUT /reservation-services/{} - Actualizar relación Reservation-Service", id);
        log.debug("Request body: {}", requestDTO);

        ReservationServiceResponseDTO response = reservationServiceServiceImpl.update(id, requestDTO);

        log.info("Relación Reservation-Service actualizada exitosamente con ID: {}", id);
        return ResponseEntity.ok(response);
    }


    // ===== DELETE (LOGICAL) =====
    @Operation(summary = "Eliminar relación (borrado lógico)",
            description = "Realiza un borrado lógico de la relación Reservation-Service (marca como inactiva)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Eliminación exitosa"),
            @ApiResponse(responseCode = "404", description = "Relación no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la relación Reservation-Service") @PathVariable Long id) {

        log.info("DELETE /reservation-services/{} - Eliminar relación", id);

        reservationServiceServiceImpl.delete(id);

        log.info("Relación ID: {} eliminada exitosamente", id);
        return ResponseEntity.noContent().build();
    }

    // ===== DELETE ALL BY RESERVATION =====
    @Operation(summary = "Eliminar todas las relaciones de una reservación",
            description = "Elimina todas las relaciones Reservation-Service asociadas a una reservación específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Eliminación exitosa"),
            @ApiResponse(responseCode = "404", description = "Relación no encontrada")
    })@DeleteMapping("/by-reservation/{reservationId}")
    public ResponseEntity<Void> deleteAllByReservationId(@PathVariable Long reservationId) {

        log.info("DELETE /reservation-services/by-reservation/{} - Eliminar todas las relaciones", reservationId);

        reservationServiceServiceImpl.deleteAllByReservationId(reservationId);

        log.info("Relaciones de la reservación {} eliminadas exitosamente", reservationId);
        return ResponseEntity.noContent().build();
    }
}
