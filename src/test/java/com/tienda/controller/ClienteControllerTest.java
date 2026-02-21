package com.tienda.controller;

import com.tienda.dto.ClienteCreateRequest;
import com.tienda.dto.ClienteResponse;
import com.tienda.exception.GlobalExceptionHandler;
import com.tienda.exception.ResourceNotFoundException;
import com.tienda.service.ClienteService;
import com.tienda.service.CotizacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClienteController.class)
@Import(GlobalExceptionHandler.class) // para tener cuerpo con mensaje en 404, 400, etc.
class ClienteControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper mapper;

    @MockitoBean private ClienteService clienteService;
    @MockitoBean private CotizacionService cotizacionService;

    @Test
    void crearCliente_deberiaRetornar201() throws Exception {
        var req = new ClienteCreateRequest(
                "Juan",
                "Pérez",
                "juan.perez@example.com",
                "3001234567",
                "123"
        );

        var res = ClienteResponse.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan.perez@example.com")
                .telefono("3001234567")
                .documento("123")
                .createdAt(LocalDateTime.parse("2026-02-12T15:30:20"))
                .build();

        when(clienteService.crearCliente(any())).thenReturn(res);

        mvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerCliente_deberiaRetornar404CuandoNoExiste() throws Exception {
        when(clienteService.obtenerClientePorId(999L))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 999"));

        mvc.perform(get("/api/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Cliente no encontrado")));
    }
}