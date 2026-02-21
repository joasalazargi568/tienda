package com.tienda.service;

import com.tienda.dto.CotizacionCreateRequest;
import com.tienda.dto.CotizacionResponse;
import com.tienda.dto.PageResponse;
import com.tienda.exception.ResourceNotFoundException;
import com.tienda.model.Cliente;
import com.tienda.model.Cotizacion;
import com.tienda.model.EstadoCotizacion;
import com.tienda.repository.ClienteRepository;
import com.tienda.repository.CotizacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private CotizacionService cotizacionService;

    private Cliente clienteExistente;
    private CotizacionCreateRequest requestValida;

    @BeforeEach
    void setUp() {
        clienteExistente = Cliente.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan.perez@example.com")
                .build();

        requestValida = CotizacionCreateRequest.builder()
                .clienteId(1L)
                .total(new BigDecimal("259900.00"))
                .build();
    }

    @Test
    void crearCotizacion_deberiaCrearConEstadoInicialCREADA_yMapearDTO() {
        // arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(cotizacionRepository.save(any(Cotizacion.class)))
                .thenAnswer(inv -> {
                    Cotizacion c = inv.getArgument(0);
                    c.setId(42L);
                    c.setCreatedAt(LocalDateTime.parse("2026-02-12T15:27:45"));
                    // Aseguramos que se mantenga el cliente seteado por el service
                    return c;
                });

        // act
        CotizacionResponse res = cotizacionService.crearCotizacion(requestValida);

        // assert
        assertThat(res.getId()).isEqualTo(42L);
        assertThat(res.getClienteId()).isEqualTo(1L);
        assertThat(res.getClienteEmail()).isEqualTo("juan.perez@example.com");
        // getNombreCompleto() debería concatenar nombres + apellidos:
        assertThat(res.getClienteNombre()).contains("Juan").contains("Pérez");
        assertThat(res.getEstado()).isEqualTo(EstadoCotizacion.CREADA);
        assertThat(res.getTotal()).isEqualByComparingTo("259900.00");
        assertThat(res.getCreatedAt()).isEqualTo(LocalDateTime.parse("2026-02-12T15:27:45"));

        // verificaciones adicionales sobre el entity guardado
        ArgumentCaptor<Cotizacion> captor = ArgumentCaptor.forClass(Cotizacion.class);
        verify(cotizacionRepository).save(captor.capture());
        Cotizacion guardada = captor.getValue();
        assertThat(guardada.getCliente().getId()).isEqualTo(1L);
        assertThat(guardada.getEstado()).isEqualTo(EstadoCotizacion.CREADA);
        assertThat(guardada.getTotal()).isEqualByComparingTo("259900.00");
    }

    @Test
    void crearCotizacion_deberiaLanzarNotFoundCuandoClienteNoExiste() {
        // arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> cotizacionService.crearCotizacion(requestValida))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con id: 1");

        verify(cotizacionRepository, never()).save(any());
    }

    @Test
    void listarPorClientePaginado_deberiaRetornarPageResponseMapeado() {
        // arrange
        Long clienteId = 1L;
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Order.desc("createdAt")));

        // El servicio valida existencia del cliente
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteExistente));

        // Construimos 2 cotizaciones para la página 0
        Cotizacion q1 = Cotizacion.builder()
                .id(10L)
                .cliente(clienteExistente)
                .total(new BigDecimal("100000.00"))
                .estado(EstadoCotizacion.CREADA)
                .createdAt(LocalDateTime.parse("2026-02-12T10:00:00"))
                .build();

        Cotizacion q2 = Cotizacion.builder()
                .id(11L)
                .cliente(clienteExistente)
                .total(new BigDecimal("159900.00"))
                .estado(EstadoCotizacion.CREADA)
                .createdAt(LocalDateTime.parse("2026-02-12T09:00:00"))
                .build();

        Page<Cotizacion> page = new PageImpl<>(
                List.of(q1, q2),
                pageable,
                5 // totalElements
        );

        // Según tu comentario en el service, usas la opción B:
        when(cotizacionRepository.findByClienteIdWithCliente(eq(clienteId), any(Pageable.class)))
                .thenReturn(page);

        // act
        PageResponse<CotizacionResponse> res = cotizacionService.listarPorClientePaginado(clienteId, pageable);

        // assert
        assertThat(res.getContent()).hasSize(2);
        assertThat(res.getPage()).isEqualTo(0);
        assertThat(res.getSize()).isEqualTo(2);
        assertThat(res.getTotalElements()).isEqualTo(5);
        assertThat(res.getTotalPages()).isEqualTo((int) Math.ceil(5 / 2.0)); // 3
        assertThat(res.isLast()).isFalse();

        // Validamos mapeo de los items
        CotizacionResponse r1 = res.getContent().get(0);
        assertThat(r1.getId()).isEqualTo(10L);
        assertThat(r1.getClienteId()).isEqualTo(1L);
        assertThat(r1.getClienteEmail()).isEqualTo("juan.perez@example.com");
        assertThat(r1.getEstado()).isEqualTo(EstadoCotizacion.CREADA);
        assertThat(r1.getTotal()).isEqualByComparingTo("100000.00");
        assertThat(r1.getCreatedAt()).isEqualTo(LocalDateTime.parse("2026-02-12T10:00:00"));

        CotizacionResponse r2 = res.getContent().get(1);
        assertThat(r2.getId()).isEqualTo(11L);
        assertThat(r2.getTotal()).isEqualByComparingTo("159900.00");
        assertThat(r2.getCreatedAt()).isEqualTo(LocalDateTime.parse("2026-02-12T09:00:00"));
    }

    @Test
    void listarPorClientePaginado_deberiaLanzarNotFoundSiClienteNoExiste() {
        // arrange
        Long clienteId = 999L;
        Pageable pageable = PageRequest.of(0, 10);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> cotizacionService.listarPorClientePaginado(clienteId, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con id: 999");

        verify(cotizacionRepository, never()).findByClienteIdWithCliente(anyLong(), any());
    }

    @Test
    void listarPorClientePaginado_deberiaPropagarOrdenamientoYPageableAlRepository() {
        // arrange
        Long clienteId = 1L;
        Pageable pageable = PageRequest.of(1, 20, Sort.by(Sort.Order.asc("total"), Sort.Order.desc("createdAt")));
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteExistente));

        Page<Cotizacion> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(cotizacionRepository.findByClienteIdWithCliente(eq(clienteId), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable recibido = inv.getArgument(1);
                    // Validamos que el pageable llega intacto (paginación y sort)
                    assertThat(recibido.getPageNumber()).isEqualTo(1);
                    assertThat(recibido.getPageSize()).isEqualTo(20);
                    assertThat(recibido.getSort().getOrderFor("total").getDirection()).isEqualTo(Sort.Direction.ASC);
                    assertThat(recibido.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
                    return emptyPage;
                });

        // act
        PageResponse<CotizacionResponse> res = cotizacionService.listarPorClientePaginado(clienteId, pageable);

        // assert
        assertThat(res.getContent()).isEmpty();
        assertThat(res.getPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(20);
        assertThat(res.getTotalElements()).isEqualTo(0);
        assertThat(res.getTotalPages()).isEqualTo(0);
        assertThat(res.isLast()).isTrue(); // una page vacía con total 0 se considera last
    }
}