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
@Table(name = "viaje_paradas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViajeParada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasajero_id", nullable = false)
    private Pasajero pasajero;

    @Column(name = "orden_parada", nullable = false)
    private Integer ordenParada;

    @Column(name = "distancia_acumulada_m")
    private Double distanciaAcumuladaM;

    @Column(name = "tiempo_estimado_seg")
    private Integer tiempoEstimadoSeg;

    // Snapshot de la ubicación del pasajero al momento de optimizar (puede diferir de la dirección actual).
    @Column(name = "latitud_snapshot")
    private Double latitudSnapshot;

    @Column(name = "longitud_snapshot")
    private Double longitudSnapshot;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
