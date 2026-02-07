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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public CotizacionResponse crearCotizacion(CotizacionCreateRequest request) {

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + request.getClienteId()
                ));

        Cotizacion cotizacion = Cotizacion.builder()
                .cliente(cliente)
                .total(request.getTotal())
                .estado(EstadoCotizacion.CREADA)
                .build();

        Cotizacion guardada = cotizacionRepository.save(cotizacion);

        return toResponse(guardada);
    }

    private CotizacionResponse toResponse(Cotizacion c) {
        return CotizacionResponse.builder()
                .id(c.getId())
                .clienteId(c.getCliente().getId())
                .clienteNombre(c.getCliente().getNombreCompleto())
                .clienteEmail(c.getCliente().getEmail())
                .total(c.getTotal())
                .estado(c.getEstado())
                .salesforceQuoteId(c.getSalesforceQuoteId())
                .createdAt(c.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<CotizacionResponse> listarPorClientePaginado(Long clienteId, Pageable pageable) {

        // 1) Validar que el cliente exista para retornar 404 si no existe
        clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + clienteId));

        // 2) Obtener Page desde repository
        // Si implementaste Opción A:
        // Page<Cotizacion> page = cotizacionRepository.findByClienteId(clienteId, pageable);

        // Si implementaste Opción B (recomendada con Bonus):
        Page<Cotizacion> page = cotizacionRepository.findByClienteIdWithCliente(clienteId, pageable);

        // 3) Mapear a DTO
        return PageResponse.<CotizacionResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}