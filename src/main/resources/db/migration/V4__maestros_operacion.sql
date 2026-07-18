-- Etapa 1 (maestros de operación): vehículos, geografía configurable (comunas/sectores),
-- rutas, turnos configurables, estados de asistencia configurables, parámetros de operación,
-- campos nuevos de pasajero/conductor y rol OPERADOR.

-- ---------------------------------------------------------------------------
-- Geografía configurable: comunas agrupadas en sectores, combinables en rutas.
-- ---------------------------------------------------------------------------
CREATE TABLE comunas (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    activo      BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE sectores (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre       VARCHAR(100) NOT NULL UNIQUE,
    descripcion  TEXT,
    activo       BOOLEAN NOT NULL DEFAULT true,
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE sector_comunas (
    sector_id  UUID NOT NULL REFERENCES sectores (id) ON DELETE CASCADE,
    comuna_id  UUID NOT NULL REFERENCES comunas (id),
    PRIMARY KEY (sector_id, comuna_id)
);

-- ---------------------------------------------------------------------------
-- Vehículos como entidad propia (antes la patente era solo texto en conductor).
-- ---------------------------------------------------------------------------
CREATE TABLE vehiculos (
    id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patente                    VARCHAR(20)  NOT NULL UNIQUE,
    marca                      VARCHAR(100),
    modelo                     VARCHAR(100),
    anio                       INTEGER,
    capacidad_pasajeros        INTEGER NOT NULL,
    tipo_vehiculo              VARCHAR(50),
    estado                     VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE'
                               CHECK (estado IN ('DISPONIBLE', 'EN_MANTENCION', 'FUERA_DE_SERVICIO')),
    conductor_habitual_id      UUID REFERENCES conductores (id),
    kilometraje                INTEGER,
    fecha_revision_tecnica     DATE,
    fecha_permiso_circulacion  DATE,
    fecha_vencimiento_seguro   DATE,
    observaciones              TEXT,
    activo                     BOOLEAN NOT NULL DEFAULT true,
    created_at                 TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Rutas: plantilla operativa por empresa que agrupa sectores, con conductor y
-- vehículo habituales (la asignación real por recorrido puede diferir y queda
-- registrada en el viaje — Etapa 3).
-- ---------------------------------------------------------------------------
CREATE TABLE rutas (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id             UUID NOT NULL REFERENCES empresas_clientes (id),
    nombre                 VARCHAR(150) NOT NULL,
    descripcion            TEXT,
    conductor_habitual_id  UUID REFERENCES conductores (id),
    vehiculo_habitual_id   UUID REFERENCES vehiculos (id),
    activo                 BOOLEAN NOT NULL DEFAULT true,
    created_at             TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_ruta_empresa_nombre UNIQUE (empresa_id, nombre)
);

CREATE TABLE ruta_sectores (
    ruta_id    UUID NOT NULL REFERENCES rutas (id) ON DELETE CASCADE,
    sector_id  UUID NOT NULL REFERENCES sectores (id),
    PRIMARY KEY (ruta_id, sector_id)
);

-- ---------------------------------------------------------------------------
-- Turnos configurables: nombre + tipo de servicio (entrada/salida), horas y
-- días de funcionamiento. Ej: "Mañana"/ENTRADA = "Entrada turno mañana".
-- dias_semana: códigos separados por coma (LU,MA,MI,JU,VI,SA,DO).
-- ---------------------------------------------------------------------------
CREATE TABLE turnos (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre                 VARCHAR(100) NOT NULL,
    tipo_servicio          VARCHAR(20) NOT NULL CHECK (tipo_servicio IN ('ENTRADA', 'SALIDA')),
    hora_inicio            TIME NOT NULL,
    hora_llegada_estimada  TIME,
    dias_semana            VARCHAR(50),
    activo                 BOOLEAN NOT NULL DEFAULT true,
    created_at             TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_turno_nombre_tipo UNIQUE (nombre, tipo_servicio)
);

-- ---------------------------------------------------------------------------
-- Estados de asistencia configurables (se agregan sin tocar código).
-- requiere_observacion: obliga a ingresar observación al marcar (ej: OTRO).
-- ---------------------------------------------------------------------------
CREATE TABLE estados_asistencia (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo                VARCHAR(40) NOT NULL UNIQUE,
    nombre                VARCHAR(100) NOT NULL,
    requiere_observacion  BOOLEAN NOT NULL DEFAULT false,
    orden                 INTEGER NOT NULL DEFAULT 0,
    activo                BOOLEAN NOT NULL DEFAULT true,
    created_at            TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO estados_asistencia (codigo, nombre, requiere_observacion, orden) VALUES
    ('ASISTIO',               'Asistió',                              false, 1),
    ('NO_ASISTIO',            'No asistió',                           false, 2),
    ('AVISO_PREVIO',          'Avisó que no utilizaría el transporte', false, 3),
    ('NO_UTILIZA_TRANSPORTE', 'No utiliza transporte',                false, 4),
    ('MEDIOS_PROPIOS',        'Llegó por sus propios medios',         false, 5),
    ('OTRO',                  'Otro',                                 true,  6);

-- ---------------------------------------------------------------------------
-- Parámetros de operación configurables (clave/valor).
-- ---------------------------------------------------------------------------
CREATE TABLE configuraciones (
    clave        VARCHAR(80) PRIMARY KEY,
    valor        VARCHAR(255) NOT NULL,
    descripcion  TEXT,
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO configuraciones (clave, valor, descripcion) VALUES
    ('MINUTOS_MINIMOS_ENTRE_RECORRIDOS', '30',
     'Tiempo mínimo (minutos) entre el término de un recorrido y el inicio del siguiente para un mismo vehículo o conductor');

-- ---------------------------------------------------------------------------
-- Pasajeros: uso del servicio y habituales (turno / sector / ruta).
-- ---------------------------------------------------------------------------
ALTER TABLE pasajeros
    ADD COLUMN utiliza_transporte VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (utiliza_transporte IN ('SI', 'NO', 'OCASIONAL', 'PENDIENTE')),
    ADD COLUMN sector_id UUID REFERENCES sectores (id),
    ADD COLUMN ruta_habitual_id UUID REFERENCES rutas (id),
    ADD COLUMN turno_habitual_id UUID REFERENCES turnos (id),
    ADD COLUMN observaciones TEXT;

-- ---------------------------------------------------------------------------
-- Conductores: ficha completa (correo, licencia y vencimiento, observaciones).
-- patente_vehiculo se conserva como dato legado hasta migrar a vehiculos.
-- ---------------------------------------------------------------------------
ALTER TABLE conductores
    ADD COLUMN email VARCHAR(255),
    ADD COLUMN tipo_licencia VARCHAR(20),
    ADD COLUMN fecha_vencimiento_licencia DATE,
    ADD COLUMN observaciones TEXT;

-- ---------------------------------------------------------------------------
-- Rol OPERADOR: planifica (importa, arma recorridos, asigna) pero no administra
-- maestros ni configuración.
-- ---------------------------------------------------------------------------
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_rol_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_rol_check
    CHECK (rol IN ('ADMIN', 'OPERADOR', 'EMPRESA', 'PASAJERO', 'CONDUCTOR'));
