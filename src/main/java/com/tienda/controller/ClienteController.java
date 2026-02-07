package com.tienda.controller;

import com.tienda.dto.ClienteCreateRequest;
import com.tienda.dto.ClienteResponse;
import com.tienda.dto.CotizacionResponse;
import com.tienda.service.ClienteService;
import com.tienda.service.CotizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tienda.dto.CotizacionResponse;
import com.tienda.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final CotizacionService cotizacionService;

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteCreateRequest request) {
        ClienteResponse creado = clienteService.crearCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable Long id) {
        ClienteResponse cliente = clienteService.obtenerClientePorId(id);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/{id}/cotizaciones")
    public ResponseEntity<PageResponse<CotizacionResponse>> listarCotizacionesPorCliente(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        int maxSize = 50;
        if (pageable.getPageSize() > maxSize) {
            pageable = Pageable.ofSize(maxSize).withPage(pageable.getPageNumber());
        }
        return ResponseEntity.ok(cotizacionService.listarPorClientePaginado(id, pageable));
    }
}