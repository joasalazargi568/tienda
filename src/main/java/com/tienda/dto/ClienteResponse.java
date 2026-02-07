package com.tienda.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponse {
    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private String documento;
    private String salesforceAccountId;
    private LocalDateTime createdAt;
}