-- Etapa 4 (operación del conductor): estados de asistencia configurables en el
-- checklist, correcciones auditadas e incidencias.

-- ---------------------------------------------------------------------------
-- El estado de asistencia deja de ser un enum fijo: ahora valida contra la
-- tabla configurable estados_asistencia (más el valor inicial PENDIENTE).
-- ---------------------------------------------------------------------------
ALTER TABLE asistencia_checklist DROP CONSTRAINT IF EXISTS asistencia_checklist_estado_check;
ALTER TABLE asistencia_checklist ALTER COLUMN estado TYPE VARCHAR(40);

-- Migración de los códigos legados al catálogo sembrado en V4.
UPDATE asistencia_checklist SET estado = 'ASISTIO'        WHERE estado = 'SUBIO';
UPDATE asistencia_checklist SET estado = 'NO_ASISTIO'     WHERE estado = 'NO_SHOW';
UPDATE asistencia_checklist SET estado = 'MEDIOS_PROPIOS' WHERE estado = 'CUENTA_PROPIA';

-- ---------------------------------------------------------------------------
-- Correcciones de asistencia con historial (sección 13 del requerimiento).
-- ---------------------------------------------------------------------------
CREATE TABLE asistencia_historial (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asistencia_id   UUID NOT NULL REFERENCES asistencia_checklist (id) ON DELETE CASCADE,
    valor_anterior  VARCHAR(40) NOT NULL,
    valor_nuevo     VARCHAR(40) NOT NULL,
    motivo          TEXT NOT NULL,
    usuario_id      UUID,
    usuario_rol     VARCHAR(20),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_asistencia_historial_asistencia ON asistencia_historial (asistencia_id);

-- ---------------------------------------------------------------------------
-- Incidencias asociadas a recorrido / pasajero / conductor / vehículo (sección 11).
-- ---------------------------------------------------------------------------
CREATE TABLE incidencias (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    viaje_id          UUID REFERENCES viajes (id),
    pasajero_id       UUID REFERENCES pasajeros (id),
    conductor_id      UUID REFERENCES conductores (id),
    vehiculo_id       UUID REFERENCES vehiculos (id),
    tipo              VARCHAR(50) NOT NULL,
    descripcion       TEXT NOT NULL,
    estado            VARCHAR(20) NOT NULL DEFAULT 'ABIERTA'
                      CHECK (estado IN ('ABIERTA', 'EN_GESTION', 'CERRADA')),
    accion_realizada  TEXT,
    reportado_por     UUID,
    reportado_rol     VARCHAR(20),
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_incidencias_viaje ON incidencias (viaje_id);
CREATE INDEX idx_incidencias_estado ON incidencias (estado);
