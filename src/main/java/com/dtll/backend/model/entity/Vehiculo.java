package com.dtll.backend.model.entity;

import com.dtll.backend.model.enums.EstadoVehiculo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patente", length = 20, nullable = false, unique = true)
    private String patente;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "capacidad_pasajeros", nullable = false)
    private Integer capacidadPasajeros;

    @Column(name = "tipo_vehiculo", length = 50)
    private String tipoVehiculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30, nullable = false)
    @Builder.Default
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conductor_habitual_id")
    private Conductor conductorHabitual;

    @Column(name = "kilometraje")
    private Integer kilometraje;

    @Column(name = "fecha_revision_tecnica")
    private LocalDate fechaRevisionTecnica;

    @Column(name = "fecha_permiso_circulacion")
    private LocalDate fechaPermisoCirculacion;

    @Column(name = "fecha_vencimiento_seguro")
    private LocalDate fechaVencimientoSeguro;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Alguno de los tres documentos (revisión técnica, permiso, seguro) está vencido a la fecha dada. */
    public boolean documentosVencidos(LocalDate hoy) {
        return esFechaVencida(fechaRevisionTecnica, hoy)
                || esFechaVencida(fechaPermisoCirculacion, hoy)
                || esFechaVencida(fechaVencimientoSeguro, hoy);
    }

    private boolean esFechaVencida(LocalDate fecha, LocalDate hoy) {
        return fecha != null && fecha.isBefore(hoy);
    }
}
