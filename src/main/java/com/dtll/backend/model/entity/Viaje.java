package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.dtll.backend.model.enums.Jornada;
import com.dtll.backend.model.enums.TipoTrayecto;
import com.dtll.backend.model.enums.EstadoViaje;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "viajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_ruta", length = 20, nullable = false, unique = true)
    private String codigoRuta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaCliente empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conductor_id", nullable = false)
    private Conductor conductor;

    @Column(name = "fecha_viaje", nullable = false)
    private LocalDate fechaViaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "jornada", length = 20)
    private Jornada jornada;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_trayecto", length = 20)
    private TipoTrayecto tipoTrayecto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoViaje estado;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
