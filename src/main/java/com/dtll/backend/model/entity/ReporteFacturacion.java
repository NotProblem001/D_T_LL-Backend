package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "reportes_facturacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteFacturacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaCliente empresaCliente;

    @Column(name = "mes_fiscal", nullable = false)
    private Integer mesFiscal;

    @Column(name = "anio_fiscal", nullable = false)
    private Integer anioFiscal;

    @Column(name = "total_viajes_ejecutados", nullable = false)
    private Integer totalViajesEjecutados;

    @Column(name = "monto_exento_total", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoExentoTotal;

    @Column(name = "estado_documento", length = 20)
    @Builder.Default
    private String estadoDocumento = "BORRADOR";
}
