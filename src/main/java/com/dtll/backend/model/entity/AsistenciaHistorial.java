package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/** Corrección de una asistencia ya marcada: valor anterior/nuevo, quién y por qué (sección 13). */
@Entity
@Table(name = "asistencia_historial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asistencia_id", nullable = false)
    private AsistenciaChecklist asistencia;

    @Column(name = "valor_anterior", length = 40, nullable = false)
    private String valorAnterior;

    @Column(name = "valor_nuevo", length = 40, nullable = false)
    private String valorNuevo;

    @Column(name = "motivo", columnDefinition = "TEXT", nullable = false)
    private String motivo;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "usuario_rol", length = 20)
    private String usuarioRol;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
