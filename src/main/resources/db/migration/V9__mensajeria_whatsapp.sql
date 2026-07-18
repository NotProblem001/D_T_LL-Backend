-- Etapa 5 (comunicación WhatsApp, sección 10): mensajes generados por recorrido
-- con registro de envío, y el grupo de WhatsApp asociado a cada ruta.

ALTER TABLE rutas ADD COLUMN grupo_whatsapp VARCHAR(255);

CREATE TABLE mensajes_ruta (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    viaje_id        UUID NOT NULL REFERENCES viajes (id) ON DELETE CASCADE,
    texto           TEXT NOT NULL,
    grupo_whatsapp  VARCHAR(255),
    enviado         BOOLEAN NOT NULL DEFAULT false,
    enviado_at      TIMESTAMP,
    enviado_por     UUID,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_mensajes_ruta_viaje ON mensajes_ruta (viaje_id);
