-- Etapa 3 (planificación): los viajes se generan como propuesta por turno+ruta,
-- se les asigna conductor y vehículo con validaciones, y amplían sus estados al
-- ciclo completo del requerimiento (sección 12).

-- La propuesta nace sin conductor (estado BORRADOR).
ALTER TABLE viajes ALTER COLUMN conductor_id DROP NOT NULL;

ALTER TABLE viajes
    ADD COLUMN vehiculo_id UUID REFERENCES vehiculos (id),
    ADD COLUMN ruta_id     UUID REFERENCES rutas (id),
    ADD COLUMN turno_id    UUID REFERENCES turnos (id),
    ADD COLUMN hora_programada_inicio  TIME,
    ADD COLUMN hora_programada_termino TIME,
    -- Horas reales: las registra el conductor (Etapa 4).
    ADD COLUMN hora_real_inicio  TIMESTAMP,
    ADD COLUMN hora_real_termino TIMESTAMP;

CREATE INDEX idx_viajes_vehiculo ON viajes (vehiculo_id);
CREATE INDEX idx_viajes_ruta ON viajes (ruta_id);

ALTER TABLE viajes DROP CONSTRAINT IF EXISTS viajes_estado_check;
ALTER TABLE viajes ADD CONSTRAINT viajes_estado_check
    CHECK (estado IN ('BORRADOR', 'PROGRAMADO', 'ASIGNADO', 'CONFIRMADO',
                      'EN_CURSO', 'FINALIZADO', 'CANCELADO', 'REPROGRAMADO'));
