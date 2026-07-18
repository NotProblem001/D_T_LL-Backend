package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.dtll.backend.model.enums.TipoContrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conductores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rut_conductor", length = 20, nullable = false, unique = true)
    private String rutConductor;

    @Column(name = "nombre_completo", length = 255, nullable = false)
    private String nombreCompleto;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "patente_vehiculo", length = 20)
    private String patenteVehiculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", length = 50)
    private TipoContrato tipoContrato;

    @Column(name = "tarifa_por_viaje", precision = 10, scale = 2)
    private BigDecimal tarifaPorViaje;

    @Column(name = "pin_acceso_hash", length = 255)
    private String pinAccesoHash;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "tipo_licencia", length = 20)
    private String tipoLicencia;

    @Column(name = "fecha_vencimiento_licencia")
    private LocalDate fechaVencimientoLicencia;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
