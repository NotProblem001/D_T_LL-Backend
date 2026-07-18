package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Estado de asistencia configurable (se agregan nuevos sin tocar código).
 * requiereObservacion obliga a ingresar una observación al marcar (ej: OTRO).
 */
@Entity
@Table(name = "estados_asistencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoAsistenciaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo", length = 40, nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "requiere_observacion", nullable = false)
    @Builder.Default
    private Boolean requiereObservacion = false;

    @Column(name = "orden", nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
