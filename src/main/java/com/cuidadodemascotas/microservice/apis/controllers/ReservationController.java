package com.cuidadodemascotas.microservice.apis.controllers;

import com.cuidadodemascotas.microservice.apis.services.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cuidadodemascota.commons.dto.ReservationRequestDTO;
import org.example.cuidadodemascota.commons.dto.ReservationResponseDTO;
import org.example.cuidadodemascota.commons.entities.enums.ReservationStateEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controlador REST para la gestión de Reservations
 * Expone todos los endpoints CRUD + búsquedas con filtros
 */
@Slf4j
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "API para gestión de reservaciones")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Crear una nueva reservación",
            description = "Crea una nueva reservación validando owner, carer y disponibilidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservación creada exitosamente",
                    content = @Content(schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Owner o Carer no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto de horario con el carer")
    })
    @PostMapping
    public ResponseEntity<ReservationResponseDTO> create(
            @RequestBody ReservationRequestDTO requestDTO) {

        log.info("POST /reservations - Crear nueva reservación");
        log.debug("Request body: {}", requestDTO);

        ReservationResponseDTO response = reservationService.create(requestDTO);

        log.info("Reservación creada exitosamente con ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Actualizar una reservación",
            description = "Actualiza una reservación existente por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservación actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Reservación no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> update(
            @Parameter(description = "ID de la reservación") @PathVariable Long id,
            @RequestBody ReservationRequestDTO requestDTO) {

        log.info("PUT /reservations/{} - Actualizar reservación", id);
        log.debug("Request body: {}", requestDTO);

        ReservationResponseDTO response = reservationService.update(id, requestDTO);

        log.info("Reservación ID: {} actualizada exitosamente", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener reservación por ID",
            description = "Obtiene una reservación específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservación encontrada",
                    content = @Content(schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reservación no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> findById(
            @Parameter(description = "ID de la reservación") @PathVariable Long id) {

        log.info("GET /reservations/{} - Obtener reservación por ID", id);

        ReservationResponseDTO response = reservationService.findById(id);

        log.info("Reservación ID: {} encontrada", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todas las reservaciones",
            description = "Obtiene todas las reservaciones activas con paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservaciones obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<ReservationResponseDTO>> findAll(
            @Parameter(description = "Número de página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo de ordenamiento")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (ASC/DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("GET /reservations - Listar todas (page={}, size={}, sortBy={}, sortDir={})",
                page, size, sortBy, sortDir);

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ReservationResponseDTO> response = reservationService.findAll(pageable);

        log.info("Se obtuvieron {} reservaciones de {} totales",
                response.getNumberOfElements(), response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar reservaciones con filtros",
            description = "Busca reservaciones aplicando filtros opcionales con paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ReservationResponseDTO>> search(
            @Parameter(description = "ID del propietario")
            @RequestParam(required = false) Long ownerId,
            @Parameter(description = "ID del cuidador")
            @RequestParam(required = false) Long carerId,
            @Parameter(description = "Estado de la reservación")
            @RequestParam(required = false) ReservationStateEnum state,
            @Parameter(description = "Fecha de inicio del rango")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Fecha de fin del rango")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "serviceDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("GET /reservations/search - Filtros: ownerId={}, carerId={}, state={}, startDate={}, endDate={}",
                ownerId, carerId, state, startDate, endDate);

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ReservationResponseDTO> response = reservationService.findByFilters(
                ownerId, carerId, state, startDate, endDate, pageable);

        log.info("Búsqueda completada: {} resultados encontrados", response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar reservación (borrado lógico)",
            description = "Realiza un borrado lógico de la reservación (marca como inactiva)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservación eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Reservación no encontrada"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar la reservación (validación de negocio)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la reservación") @PathVariable Long id) {

        log.info("DELETE /reservations/{} - Eliminar reservación", id);

        reservationService.delete(id);

        log.info("Reservación ID: {} eliminada exitosamente", id);
        return ResponseEntity.noContent().build();
    }
}