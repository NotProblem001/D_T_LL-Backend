-- Extensión habilitada para soportar geolocalización (índices/consultas espaciales futuras).
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE empresas_clientes (
    id                 UUID PRIMARY KEY,
    rut_fiscal         VARCHAR(20)  NOT NULL UNIQUE,
    razon_social       VARCHAR(255),
    nombre_fantasia    VARCHAR(255),
    contacto_nombre    VARCHAR(255),
    contacto_email     VARCHAR(255),
    contacto_telefono  VARCHAR(50),
    tarifa_base_viaje  NUMERIC(10, 2),
    created_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE conductores (
    id                 UUID PRIMARY KEY,
    rut_conductor      VARCHAR(20)  NOT NULL UNIQUE,
    nombre_completo    VARCHAR(255) NOT NULL,
    telefono           VARCHAR(50),
    patente_vehiculo   VARCHAR(20),
    tipo_contrato      VARCHAR(50) CHECK (tipo_contrato IN ('FIJO', 'VARIABLE', 'APOYO')),
    tarifa_por_viaje   NUMERIC(10, 2),
    pin_acceso_hash    VARCHAR(255),
    activo             BOOLEAN NOT NULL DEFAULT true,
    created_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE pasajeros (
    id                     UUID PRIMARY KEY,
    empresa_id             UUID NOT NULL REFERENCES empresas_clientes (id),
    identificador_interno  VARCHAR(50)  NOT NULL UNIQUE,
    rut                    VARCHAR(20) UNIQUE,
    nombre_completo        VARCHAR(255) NOT NULL,
    direccion_referencia   TEXT,
    punto_parada_asignado  VARCHAR(255),
    comuna                 VARCHAR(100),
    telefono               VARCHAR(50),
    latitud                DOUBLE PRECISION,
    longitud               DOUBLE PRECISION,
    activo                 BOOLEAN NOT NULL DEFAULT true,
    created_at             TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_pasajeros_empresa ON pasajeros (empresa_id);

CREATE TABLE usuarios (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    nombre          VARCHAR(255) NOT NULL,
    rol             VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'EMPRESA', 'PASAJERO', 'CONDUCTOR')),
    empresa_id      UUID REFERENCES empresas_clientes (id),
    pasajero_id     UUID REFERENCES pasajeros (id),
    proveedor_auth  VARCHAR(20)  NOT NULL DEFAULT 'LOCAL' CHECK (proveedor_auth IN ('LOCAL', 'GOOGLE', 'LINKEDIN')),
    oauth_id        VARCHAR(255),
    activo          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_usuarios_empresa ON usuarios (empresa_id);

CREATE TABLE viajes (
    id                 UUID PRIMARY KEY,
    codigo_ruta_login  VARCHAR(20)  NOT NULL UNIQUE,
    empresa_id         UUID NOT NULL REFERENCES empresas_clientes (id),
    conductor_id       UUID NOT NULL REFERENCES conductores (id),
    fecha_operacion    DATE NOT NULL,
    jornada_turno      VARCHAR(20),
    tipo_trayecto      VARCHAR(20),
    tarifa_historica   NUMERIC(10, 2),
    estado             VARCHAR(20)  NOT NULL DEFAULT 'PROGRAMADO'
                       CHECK (estado IN ('PROGRAMADO', 'EN_CURSO', 'FINALIZADO', 'CANCELADO')),
    created_at         TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_viajes_empresa ON viajes (empresa_id);
CREATE INDEX idx_viajes_conductor ON viajes (conductor_id);
CREATE INDEX idx_viajes_fecha ON viajes (fecha_operacion);

CREATE TABLE viaje_paradas (
    id                      UUID PRIMARY KEY,
    viaje_id                UUID NOT NULL REFERENCES viajes (id) ON DELETE CASCADE,
    pasajero_id             UUID NOT NULL REFERENCES pasajeros (id),
    orden_parada            INTEGER NOT NULL,
    distancia_acumulada_m   DOUBLE PRECISION,
    tiempo_estimado_seg     INTEGER,
    latitud_snapshot        DOUBLE PRECISION,
    longitud_snapshot       DOUBLE PRECISION,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (viaje_id, pasajero_id)
);
CREATE INDEX idx_viaje_paradas_viaje ON viaje_paradas (viaje_id);

CREATE TABLE asistencia_checklist (
    id             UUID PRIMARY KEY,
    viaje_id       UUID NOT NULL REFERENCES viajes (id) ON DELETE CASCADE,
    pasajero_id    UUID NOT NULL REFERENCES pasajeros (id),
    estado         VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                   CHECK (estado IN ('PENDIENTE', 'SUBIO', 'NO_SHOW', 'CUENTA_PROPIA')),
    hora_marcaje   TIMESTAMP,
    observaciones  TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (viaje_id, pasajero_id)
);
CREATE INDEX idx_asistencia_viaje ON asistencia_checklist (viaje_id);

CREATE TABLE viaje_tracking (
    viaje_id    UUID PRIMARY KEY REFERENCES viajes (id) ON DELETE CASCADE,
    latitud     DOUBLE PRECISION NOT NULL,
    longitud    DOUBLE PRECISION NOT NULL,
    heading     DOUBLE PRECISION,
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE reportes_facturacion (
    id                        UUID PRIMARY KEY,
    empresa_id                UUID NOT NULL REFERENCES empresas_clientes (id),
    mes_fiscal                INTEGER NOT NULL,
    anio_fiscal               INTEGER NOT NULL,
    total_viajes_ejecutados   INTEGER NOT NULL,
    monto_exento_total        NUMERIC(12, 2) NOT NULL,
    estado_documento          VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    UNIQUE (empresa_id, mes_fiscal, anio_fiscal)
);
