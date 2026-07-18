package com.dtll.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/** Mensaje generado para el grupo de WhatsApp de un recorrido, con registro de envío. */
@Entity
@Table(name = "mensajes_ruta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @Column(name = "texto", columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Column(name = "grupo_whatsapp", length = 255)
    private String grupoWhatsapp;

    @Column(name = "enviado", nullable = false)
    @Builder.Default
    private Boolean enviado = false;

    @Column(name = "enviado_at")
    private LocalDateTime enviadoAt;

    @Column(name = "enviado_por")
    private UUID enviadoPor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
