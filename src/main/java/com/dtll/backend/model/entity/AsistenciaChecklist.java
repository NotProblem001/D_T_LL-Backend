package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "asistencia_checklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasajero_id", nullable = false)
    private Pasajero pasajero;

    /**
     * Código del estado: "PENDIENTE" (inicial) o uno del maestro configurable
     * estados_asistencia (ASISTIO, NO_ASISTIO, OTRO...). Se valida en el service.
     */
    @Column(name = "estado", length = 40)
    @Builder.Default
    private String estado = ESTADO_PENDIENTE;

    public static final String ESTADO_PENDIENTE = "PENDIENTE";

    @Column(name = "hora_marcaje")
    private LocalDateTime horaMarcaje;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
