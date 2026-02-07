package com.tienda.dto;

import com.tienda.model.EstadoCotizacion;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotizacionResponse {

    private Long id;
    private Long clienteId;
    private BigDecimal total;
    private EstadoCotizacion estado;
    private String salesforceQuoteId;
    private LocalDateTime createdAt;
    private String clienteNombre;
    private String clienteEmail;
}