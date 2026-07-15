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
    @JoinColumn(name = "conductor_id", nullable = false)
    private Conductor conductor;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion;

    // Texto libre proveniente del Excel; se valida contra los enums Jornada/TipoTrayecto en el service.
    @Column(name = "jornada_turno", length = 20)
    private String jornadaTurno;

    @Column(name = "tipo_trayecto", length = 20)
    private String tipoTrayecto;

    @Column(name = "tarifa_historica", precision = 10, scale = 2)
    private BigDecimal tarifaHistorica;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Builder.Default
    private EstadoViaje estado = EstadoViaje.PROGRAMADO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
