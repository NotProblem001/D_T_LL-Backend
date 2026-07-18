package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/** Reemplazo de conductor o vehículo en un recorrido: quién, cuándo y por qué (sección 8/14). */
@Entity
@Table(name = "viaje_cambios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViajeCambio {

    public static final String CAMPO_CONDUCTOR = "CONDUCTOR";
    public static final String CAMPO_VEHICULO = "VEHICULO";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @Column(name = "campo", length = 20, nullable = false)
    private String campo;

    @Column(name = "valor_anterior", length = 255)
    private String valorAnterior;

    @Column(name = "valor_nuevo", length = 255)
    private String valorNuevo;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "usuario_rol", length = 20)
    private String usuarioRol;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
