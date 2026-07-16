-- Nómina semanal de turnos enviada por las empresas cliente (ej: "SEM 29.xlsx").
-- Permite cruzar los nombres del turno con la BDD de pasajeros y generar la
-- planilla de horarios semanal sin trabajo manual.

-- Nombre normalizado (mayúsculas, sin tildes, espacios colapsados) para
-- deduplicar pasajeros por empresa al importar.
ALTER TABLE pasajeros ADD COLUMN nombre_normalizado VARCHAR(255);

-- Backfill aproximado para registros existentes (la app lo recalcula al guardar).
UPDATE pasajeros
SET nombre_normalizado = upper(trim(regexp_replace(
        translate(nombre_completo, 'áéíóúÁÉÍÓÚñÑüÜàèìòù', 'aeiouAEIOUnNuUaeiou'),
        '\s+', ' ', 'g')));

CREATE INDEX idx_pasajeros_empresa_nombre_norm
    ON pasajeros (empresa_id, nombre_normalizado);

CREATE TABLE nomina_turnos (
    id                  UUID PRIMARY KEY,
    empresa_id          UUID NOT NULL REFERENCES empresas_clientes (id),
    anio                INT NOT NULL,
    semana              INT NOT NULL,
    turno               VARCHAR(20) NOT NULL, -- MANANA | TARDE | NOCHE
    pasajero_id         UUID REFERENCES pasajeros (id),
    nombre_original     VARCHAR(255) NOT NULL,
    nombre_normalizado  VARCHAR(255) NOT NULL,
    centro_costo        VARCHAR(150),
    cargo               VARCHAR(150),
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_nomina_turno UNIQUE (empresa_id, anio, semana, turno, nombre_normalizado)
);
CREATE INDEX idx_nomina_turnos_empresa_semana ON nomina_turnos (empresa_id, anio, semana);
