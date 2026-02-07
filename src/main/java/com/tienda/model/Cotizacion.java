package com.tienda.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cotizacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación: Muchas cotizaciones pertenecen a un cliente
     * En BD: cotizacion.cliente_id (FK) -> cliente.id
     */
    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El total debe ser mayor a 0")
    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    /**
     * Se recomienda persistir el enum como STRING para evitar problemas
     * si algún día cambias el orden del enum.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoCotizacion estado = EstadoCotizacion.CREADA;

    /**
     * Id del Quote en Salesforce (15 o 18 caracteres)
     */
    @Column(name = "salesforce_quote_id", length = 18)
    private String salesforceQuoteId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoCotizacion.CREADA;
        }
    }
}