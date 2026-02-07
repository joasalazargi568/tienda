package com.tienda.controller;

import com.tienda.dto.CotizacionCreateRequest;
import com.tienda.dto.CotizacionResponse;
import com.tienda.service.CotizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @PostMapping
    public ResponseEntity<CotizacionResponse> crear(@Valid @RequestBody CotizacionCreateRequest request) {
        CotizacionResponse creada = cotizacionService.crearCotizacion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}