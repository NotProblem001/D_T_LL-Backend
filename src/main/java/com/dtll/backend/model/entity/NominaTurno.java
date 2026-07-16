package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Una persona asignada a un turno (MANANA/TARDE/NOCHE) en la nómina semanal
 * que envía la empresa cliente. Se cruza contra la BDD de pasajeros.
 */
@Entity
@Table(name = "nomina_turnos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NominaTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaCliente empresaCliente;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "semana", nullable = false)
    private Integer semana;

    /** MANANA | TARDE | NOCHE */
    @Column(name = "turno", length = 20, nullable = false)
    private String turno;

    /** Pasajero de la BDD si se logró cruzar por nombre; null si no hay match. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasajero_id")
    private Pasajero pasajero;

    @Column(name = "nombre_original", length = 255, nullable = false)
    private String nombreOriginal;

    @Column(name = "nombre_normalizado", length = 255, nullable = false)
    private String nombreNormalizado;

    @Column(name = "centro_costo", length = 150)
    private String centroCosto;

    @Column(name = "cargo", length = 150)
    private String cargo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
