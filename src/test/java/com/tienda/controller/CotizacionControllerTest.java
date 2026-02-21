package com.tienda.controller;

import com.tienda.dto.CotizacionCreateRequest;
import com.tienda.dto.CotizacionResponse;
import com.tienda.dto.PageResponse;
import com.tienda.exception.ResourceNotFoundException;
import com.tienda.model.EstadoCotizacion;
import com.tienda.service.CotizacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CotizacionController.class)
class CotizacionControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper mapper;

    @MockitoBean private CotizacionService cotizacionService;

    @Test
    void crearCotizacion_deberiaRetornar201() throws Exception {
        var req = CotizacionCreateRequest.builder()
                .clienteId(1L)
                .total(new BigDecimal("259900.00"))
                .build();

        var res = CotizacionResponse.builder()
                .id(42L)
                .clienteId(1L)
                .clienteNombre("Juan Pérez")
                .clienteEmail("juan.perez@example.com")
                .estado(EstadoCotizacion.CREADA)
                .total(new BigDecimal("259900.00"))
                .createdAt(LocalDateTime.parse("2026-02-12T15:27:45"))
                .build();

        when(cotizacionService.crearCotizacion(any())).thenReturn(res);

        mvc.perform(post("/api/cotizaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.estado").value("CREADA"));
    }

    @Test
    void crearCotizacion_deberiaRetornar400CuandoTotalNoEsPositivo() throws Exception {
        var req = CotizacionCreateRequest.builder()
                .clienteId(1L)
                .total(new BigDecimal("0.00")) // viola @DecimalMin(inclusive=false)
                .build();

        mvc.perform(post("/api/cotizaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearCotizacion_deberiaRetornar404CuandoClienteNoExiste() throws Exception {
        var req = CotizacionCreateRequest.builder()
                .clienteId(999L)
                .total(new BigDecimal("1000.00"))
                .build();

        when(cotizacionService.crearCotizacion(any()))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 999"));

        mvc.perform(post("/api/cotizaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorClientePaginado_deberiaRetornar404SiClienteNoExiste() throws Exception {
        when(cotizacionService.listarPorClientePaginado(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 999"));

        mvc.perform(get("/api/clientes/999/cotizaciones"))
                .andExpect(status().isNotFound());
    }
}