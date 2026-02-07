package com.tienda.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cliente",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cliente_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden exceder los 100 caracteres")
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden exceder los 100 caracteres")
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 150, message = "El email no pueden exceder los 150 caracteres")
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Size(max = 30, message = "El teléfono no puede exceder los 30 caracteres")
    @Column(name = "telefono", length = 30)
    private String telefono;

    @Size(max = 50, message = "El documento no puede exceder los 50 caracteres")
    @Column(name = "documento", length = 50)
    private String documento;

    @Size(max = 18, message = "El Salesforce Account Id no puede exceder los 18 caracteres")
    @Column(name = "salesforce_account_id", length = 18)
    private String salesforceAccountId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @JsonIgnore
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private java.util.List<Cotizacion> cotizaciones;


    @PrePersist
    public void prePresist(){
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
    }

    @Transient
    public String getNombreCompleto(){
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }


}
