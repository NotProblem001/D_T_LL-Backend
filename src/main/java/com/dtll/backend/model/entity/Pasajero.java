package com.dtll.backend.model.entity;

import com.dtll.backend.model.enums.UsoTransporte;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pasajeros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pasajero {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaCliente empresaCliente;

    @Column(name = "identificador_interno", length = 50, nullable = false, unique = true)
    private String identificadorInterno;

    @Column(name = "rut", length = 20, unique = true)
    private String rut;

    @Column(name = "nombre_completo", length = 255, nullable = false)
    private String nombreCompleto;

    /** Nombre en MAYÚSCULAS sin tildes para deduplicar y cruzar nóminas (ver Normalizador). */
    @Column(name = "nombre_normalizado", length = 255)
    private String nombreNormalizado;

    @Column(name = "direccion_referencia", columnDefinition = "TEXT")
    private String direccionReferencia;

    @Column(name = "punto_parada_asignado", length = 255)
    private String puntoParadaAsignado;

    @Column(name = "comuna", length = 100)
    private String comuna;

    @Column(name = "telefono", length = 50)
    private String telefono;

    // SRS 2.4: destino de las notificaciones automáticas (Gmail/SMTP).
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    /** NO excluye al pasajero de la generación de rutas (queda en listado aparte). */
    @Enumerated(EnumType.STRING)
    @Column(name = "utiliza_transporte", length = 20, nullable = false)
    @Builder.Default
    private UsoTransporte utilizaTransporte = UsoTransporte.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_habitual_id")
    private Ruta rutaHabitual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_habitual_id")
    private Turno turnoHabitual;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
