package com.tienda.service;

import com.tienda.dto.ClienteCreateRequest;
import com.tienda.dto.ClienteResponse;
import com.tienda.exception.ResourceNotFoundException;
import com.tienda.model.Cliente;
import com.tienda.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteCreateRequest requestValida;

    @BeforeEach
    void setUp() {
        // Simula espacios y mayúsculas para verificar normalización
        requestValida = ClienteCreateRequest.builder()
                .nombres("  Juan  ")
                .apellidos("  Pérez ")
                .email("  JUAN.PEREZ@example.com ")
                .telefono(" 3001234567 ")
                .documento(" 1234567890 ")
                .build();
    }

    @Test
    void crearCliente_deberiaCrearClienteCuandoEmailNoExiste_yNormalizarDatos() {
        // arrange
        when(clienteRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.parse("2026-02-12T15:30:20"));
            return c;
        });

        // act
        ClienteResponse res = clienteService.crearCliente(requestValida);

        // assert
        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getEmail()).isEqualTo("juan.perez@example.com"); // minúsculas + trim
        assertThat(res.getNombres()).isEqualTo("Juan");
        assertThat(res.getApellidos()).isEqualTo("Pérez");
        assertThat(res.getTelefono()).isEqualTo("3001234567");
        assertThat(res.getDocumento()).isEqualTo("1234567890");

        // Verificamos que se usó el email normalizado en la búsqueda
        verify(clienteRepository).findByEmail("juan.perez@example.com");

        // Capturamos el entity para validar que lo guardado también va normalizado
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        Cliente guardado = captor.getValue();
        assertThat(guardado.getEmail()).isEqualTo("juan.perez@example.com");
        assertThat(guardado.getNombres()).isEqualTo("Juan");
        assertThat(guardado.getApellidos()).isEqualTo("Pérez");
        assertThat(guardado.getTelefono()).isEqualTo("3001234567");
        assertThat(guardado.getDocumento()).isEqualTo("1234567890");
    }

    @Test
    void crearCliente_deberiaFallarCuandoEmailDuplicado_porChequeoPrevio() {
        // arrange
        when(clienteRepository.findByEmail("juan.perez@example.com"))
                .thenReturn(Optional.of(Cliente.builder().id(99L).email("juan.perez@example.com").build()));

        // act + assert
        assertThatThrownBy(() -> clienteService.crearCliente(requestValida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un cliente con el email");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crearCliente_deberiaFallarCuandoDBLanzaDataIntegrityViolation_raceCondition() {
        // arrange
        when(clienteRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        // act + assert
        assertThatThrownBy(() -> clienteService.crearCliente(requestValida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email sea único");

        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void obtenerClientePorId_deberiaRetornarClienteCuandoExiste() {
        // arrange
        Cliente entity = Cliente.builder()
                .id(1L)
                .nombres("Ana")
                .apellidos("Ramírez")
                .email("ana.ramirez@example.com")
                .telefono("3111111111")
                .documento("ABC123")
                .createdAt(LocalDateTime.parse("2026-02-12T15:30:20"))
                .build();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(entity));

        // act
        ClienteResponse res = clienteService.obtenerClientePorId(1L);

        // assert
        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getNombres()).isEqualTo("Ana");
        assertThat(res.getApellidos()).isEqualTo("Ramírez");
        assertThat(res.getEmail()).isEqualTo("ana.ramirez@example.com");
        assertThat(res.getCreatedAt())
                .isEqualTo(LocalDateTime.parse("2026-02-12T15:30:20"));
        verify(clienteRepository).findById(1L);
    }

    @Test
    void obtenerClientePorId_deberiaLanzarResourceNotFoundCuandoNoExiste() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerClientePorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con id: 999");

        verify(clienteRepository).findById(999L);
    }
}