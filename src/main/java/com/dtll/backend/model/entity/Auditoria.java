package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/** Registro de auditoría transversal (sección 21): usuario, acción, módulo y datos del cambio. */
@Entity
@Table(name = "auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "usuario_rol", length = 20)
    private String usuarioRol;

    @Column(name = "accion", length = 40, nullable = false)
    private String accion;

    @Column(name = "modulo", length = 40, nullable = false)
    private String modulo;

    @Column(name = "registro_id")
    private UUID registroId;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "datos_anterior", columnDefinition = "TEXT")
    private String datosAnterior;

    @Column(name = "datos_nuevo", columnDefinition = "TEXT")
    private String datosNuevo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
