package com.tienda.controller;

import com.tienda.dto.ClienteCreateRequest;
import com.tienda.dto.ClienteResponse;
import com.tienda.dto.CotizacionResponse;
import com.tienda.dto.PageResponse;
import com.tienda.service.ClienteService;
import com.tienda.service.CotizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final CotizacionService cotizacionService;

    // === NUEVO: POST /api/clientes ===
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteCreateRequest request) {
        ClienteResponse creado = clienteService.crearCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // === NUEVO: GET /api/clientes/{id} ===
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> obtener(@PathVariable Long id) {
        ClienteResponse resp = clienteService.obtenerClientePorId(id);
        return ResponseEntity.ok(resp);
    }

    // === EXISTENTE: GET /api/clientes/{id}/cotizaciones ===
    @GetMapping("/{id}/cotizaciones")
    public ResponseEntity<PageResponse<CotizacionResponse>> listarCotizacionesPorCliente(
            @PathVariable Long id,
            Pageable pageable
    ) {
        int maxSize = 50;
        if (pageable.getPageSize() > maxSize) {
            pageable = PageRequest.of(pageable.getPageNumber(), maxSize, pageable.getSort());
        }
        PageResponse<CotizacionResponse> page = cotizacionService.listarPorClientePaginado(id, pageable);
        return ResponseEntity.ok(page);
    }
}