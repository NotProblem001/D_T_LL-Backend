-- Etapa 6 (secciones 14, 15 y 21): historial inmutable de recorridos.
-- Al cerrar un recorrido (finalizado o cancelado) se congela un snapshot de
-- conductor, vehículo, ruta y pasajeros: los cambios posteriores en los
-- maestros no alteran lo que quedó registrado del servicio.

ALTER TABLE viajes
    ADD COLUMN conductor_nombre_snapshot   VARCHAR(255),
    ADD COLUMN conductor_rut_snapshot      VARCHAR(20),
    ADD COLUMN vehiculo_patente_snapshot   VARCHAR(20),
    ADD COLUMN vehiculo_capacidad_snapshot INT,
    ADD COLUMN ruta_nombre_snapshot        VARCHAR(150),
    ADD COLUMN total_pasajeros_snapshot    INT,
    ADD COLUMN total_transportados_snapshot INT,
    ADD COLUMN total_ausentes_snapshot     INT,
    ADD COLUMN total_cancelaciones_snapshot INT;

ALTER TABLE asistencia_checklist
    ADD COLUMN pasajero_nombre_snapshot    VARCHAR(255),
    ADD COLUMN pasajero_telefono_snapshot  VARCHAR(50),
    ADD COLUMN pasajero_direccion_snapshot TEXT;

-- ---------------------------------------------------------------------------
-- Cambios de conductor/vehículo de un recorrido: valor anterior, nuevo,
-- motivo, usuario y fecha (sección 8/14).
-- ---------------------------------------------------------------------------
CREATE TABLE viaje_cambios (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    viaje_id        UUID NOT NULL REFERENCES viajes (id) ON DELETE CASCADE,
    campo           VARCHAR(20) NOT NULL CHECK (campo IN ('CONDUCTOR', 'VEHICULO')),
    valor_anterior  VARCHAR(255),
    valor_nuevo     VARCHAR(255),
    motivo          TEXT,
    usuario_id      UUID,
    usuario_rol     VARCHAR(20),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_viaje_cambios_viaje ON viaje_cambios (viaje_id);

-- ---------------------------------------------------------------------------
-- Auditoría transversal de acciones relevantes (sección 21).
-- ---------------------------------------------------------------------------
CREATE TABLE auditoria (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID,
    usuario_rol     VARCHAR(20),
    accion          VARCHAR(40) NOT NULL,
    modulo          VARCHAR(40) NOT NULL,
    registro_id     UUID,
    descripcion     TEXT,
    datos_anterior  TEXT,
    datos_nuevo     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_auditoria_modulo ON auditoria (modulo, created_at DESC);
CREATE INDEX idx_auditoria_registro ON auditoria (registro_id);
