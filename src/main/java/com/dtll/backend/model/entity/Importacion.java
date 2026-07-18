package com.dtll.backend.model.entity;

import com.dtll.backend.model.enums.EstadoImportacion;
import com.dtll.backend.model.enums.TipoImportacion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/** Cabecera de una importación en staging: se revisa y confirma antes de tocar la BDD. */
@Entity
@Table(name = "importaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Importacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaCliente empresaCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoImportacion tipo;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "semana", nullable = false)
    private Integer semana;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    @Builder.Default
    private EstadoImportacion estado = EstadoImportacion.BORRADOR;

    @Column(name = "total_registros", nullable = false)
    @Builder.Default
    private Integer totalRegistros = 0;

    @Column(name = "total_encontrados", nullable = false)
    @Builder.Default
    private Integer totalEncontrados = 0;

    @Column(name = "total_sugerencias", nullable = false)
    @Builder.Default
    private Integer totalSugerencias = 0;

    @Column(name = "total_nuevos", nullable = false)
    @Builder.Default
    private Integer totalNuevos = 0;

    @Column(name = "total_duplicados", nullable = false)
    @Builder.Default
    private Integer totalDuplicados = 0;

    @Column(name = "total_errores", nullable = false)
    @Builder.Default
    private Integer totalErrores = 0;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmada_at")
    private LocalDateTime confirmadaAt;
}
