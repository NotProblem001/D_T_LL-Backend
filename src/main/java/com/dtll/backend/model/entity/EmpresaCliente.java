package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "empresas_clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rut_fiscal", length = 20, nullable = false, unique = true)
    private String rutFiscal;

    @Column(name = "razon_social", length = 255)
    private String razonSocial;

    @Column(name = "nombre_fantasia", length = 255)
    private String nombreFantasia;

    @Column(name = "contacto_nombre", length = 255)
    private String contactoNombre;

    @Column(name = "contacto_email", length = 255)
    private String contactoEmail;

    @Column(name = "contacto_telefono", length = 50)
    private String contactoTelefono;

    @Column(name = "tarifa_base_viaje", precision = 10, scale = 2)
    private BigDecimal tarifaBaseViaje;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
