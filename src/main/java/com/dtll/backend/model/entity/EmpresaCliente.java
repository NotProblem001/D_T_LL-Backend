package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "empresas_clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rut", length = 20, nullable = false, unique = true)
    private String rut;

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

    @Column(name = "tarifa_por_viaje", precision = 10, scale = 2)
    private BigDecimal tarifaPorViaje;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
