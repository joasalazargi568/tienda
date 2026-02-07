package com.tienda.service;

import com.tienda.dto.ClienteCreateRequest;
import com.tienda.dto.ClienteResponse;
import com.tienda.exception.ResourceNotFoundException;
import com.tienda.model.Cliente;
import com.tienda.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;


    @Transactional
    public ClienteResponse crearCliente(ClienteCreateRequest request) {
        // 1) Normalizar email (muy útil)
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        // 2) Validar duplicado por email (mejor UX que esperar error de DB)
        clienteRepository.findByEmail(emailNormalizado).ifPresent(c -> {
            throw new IllegalArgumentException("Ya existe un cliente con el email: " + emailNormalizado);
        });

        // 3) Mapear DTO -> Entity
        Cliente cliente = Cliente.builder()
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .email(emailNormalizado)
                .telefono(request.getTelefono() != null ? request.getTelefono().trim() : null)
                .documento(request.getDocumento() != null ? request.getDocumento().trim() : null)
                .build();

        // 4) Guardar
        try {
            Cliente guardado = clienteRepository.save(cliente);
            return toResponse(guardado);
        } catch (DataIntegrityViolationException e) {
            // por si se cuela el duplicado (race condition)
            throw new IllegalArgumentException("No se pudo crear el cliente. Verifica que el email sea único.");
        }
    }

    private ClienteResponse toResponse(Cliente c) {
        return ClienteResponse.builder()
                .id(c.getId())
                .nombres(c.getNombres())
                .apellidos(c.getApellidos())
                .email(c.getEmail())
                .telefono(c.getTelefono())
                .documento(c.getDocumento())
                .salesforceAccountId(c.getSalesforceAccountId())
                .createdAt(c.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        return toResponse(cliente);
    }
}