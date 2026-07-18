package com.dtll.backend.model.entity;

import com.dtll.backend.model.enums.TipoTrayecto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Turno configurable: nombre + tipo de servicio (ej: "Mañana" + ENTRADA =
 * "Entrada turno mañana"). diasSemana: códigos separados por coma (LU,MA,...,DO).
 */
@Entity
@Table(name = "turnos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", length = 20, nullable = false)
    private TipoTrayecto tipoServicio;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_llegada_estimada")
    private LocalTime horaLlegadaEstimada;

    @Column(name = "dias_semana", length = 50)
    private String diasSemana;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
