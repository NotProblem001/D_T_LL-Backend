package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.dtll.backend.model.enums.TipoContrato;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "conductores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rut", length = 20, nullable = false, unique = true)
    private String rut;

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

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
