package com.dtll.backend.model.entity;

import com.dtll.backend.model.enums.ResolucionRegistro;
import com.dtll.backend.model.enums.TipoMatch;
import com.dtll.backend.model.enums.UsoTransporte;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/** Una fila importada en staging, con su resultado de matching y la resolución del operador. */
@Entity
@Table(name = "importacion_registros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportacionRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importacion_id", nullable = false)
    private Importacion importacion;

    @Column(name = "hoja_origen", length = 100)
    private String hojaOrigen;

    @Column(name = "fila_origen")
    private Integer filaOrigen;

    @Column(name = "nombre_original", length = 255, nullable = false)
    private String nombreOriginal;

    @Column(name = "nombre_normalizado", length = 255)
    private String nombreNormalizado;

    @Column(name = "turno", length = 20)
    private String turno;

    @Column(name = "centro_costo", length = 150)
    private String centroCosto;

    @Column(name = "cargo", length = 150)
    private String cargo;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "comuna", length = 100)
    private String comuna;

    @Enumerated(EnumType.STRING)
    @Column(name = "uso_transporte_detectado", length = 20)
    private UsoTransporte usoTransporteDetectado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_match", length = 20, nullable = false)
    private TipoMatch tipoMatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasajero_id")
    private Pasajero pasajero;

    /** JSON [{pasajeroId, nombre, score}] con las sugerencias ofrecidas al operador. */
    @Column(name = "candidatos_json", columnDefinition = "TEXT")
    private String candidatosJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolucion", length = 20, nullable = false)
    @Builder.Default
    private ResolucionRegistro resolucion = ResolucionRegistro.PENDIENTE;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
