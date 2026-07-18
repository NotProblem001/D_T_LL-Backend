package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.dtll.backend.model.enums.EstadoViaje;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "viajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_ruta_login", length = 20, nullable = false, unique = true)
    private String codigoRutaLogin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaCliente empresaCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conductor_id")
    private Conductor conductor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id")
    private Turno turno;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion;

    // Texto libre proveniente del Excel; se valida contra los enums Jornada/TipoTrayecto en el service.
    @Column(name = "jornada_turno", length = 20)
    private String jornadaTurno;

    @Column(name = "tipo_trayecto", length = 20)
    private String tipoTrayecto;

    @Column(name = "tarifa_historica", precision = 10, scale = 2)
    private BigDecimal tarifaHistorica;

    @Column(name = "hora_programada_inicio")
    private LocalTime horaProgramadaInicio;

    @Column(name = "hora_programada_termino")
    private LocalTime horaProgramadaTermino;

    /** Horas reales registradas por el conductor (Etapa 4). */
    @Column(name = "hora_real_inicio")
    private LocalDateTime horaRealInicio;

    @Column(name = "hora_real_termino")
    private LocalDateTime horaRealTermino;

    // --- Snapshot al cierre (finalizado/cancelado): el historial conserva lo
    // --- que existía al momento del servicio aunque cambien los maestros.
    @Column(name = "conductor_nombre_snapshot", length = 255)
    private String conductorNombreSnapshot;

    @Column(name = "conductor_rut_snapshot", length = 20)
    private String conductorRutSnapshot;

    @Column(name = "vehiculo_patente_snapshot", length = 20)
    private String vehiculoPatenteSnapshot;

    @Column(name = "vehiculo_capacidad_snapshot")
    private Integer vehiculoCapacidadSnapshot;

    @Column(name = "ruta_nombre_snapshot", length = 150)
    private String rutaNombreSnapshot;

    @Column(name = "total_pasajeros_snapshot")
    private Integer totalPasajerosSnapshot;

    @Column(name = "total_transportados_snapshot")
    private Integer totalTransportadosSnapshot;

    @Column(name = "total_ausentes_snapshot")
    private Integer totalAusentesSnapshot;

    @Column(name = "total_cancelaciones_snapshot")
    private Integer totalCancelacionesSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Builder.Default
    private EstadoViaje estado = EstadoViaje.PROGRAMADO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
