package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "viaje_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViajeTracking {

    @Id
    @Column(name = "viaje_id")
    private UUID viajeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "viaje_id")
    private Viaje viaje;

    @Column(name = "latitud", nullable = false)
    private Double latitud;

    @Column(name = "longitud", nullable = false)
    private Double longitud;

    @Column(name = "heading")
    private Double heading;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
