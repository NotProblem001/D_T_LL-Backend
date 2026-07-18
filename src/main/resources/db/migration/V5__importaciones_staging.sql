-- Etapa 2 (importación inteligente): staging de importaciones con vista previa,
-- matching con sugerencias y confirmación del operador antes de guardar.

CREATE TABLE importaciones (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id         UUID NOT NULL REFERENCES empresas_clientes (id),
    tipo               VARCHAR(20) NOT NULL CHECK (tipo IN ('NOMINA', 'PLANILLA', 'TEXTO')),
    nombre_archivo     VARCHAR(255),
    anio               INT NOT NULL,
    semana             INT NOT NULL,
    estado             VARCHAR(20) NOT NULL DEFAULT 'BORRADOR'
                       CHECK (estado IN ('BORRADOR', 'CONFIRMADA', 'DESCARTADA')),
    total_registros    INT NOT NULL DEFAULT 0,
    total_encontrados  INT NOT NULL DEFAULT 0,
    total_sugerencias  INT NOT NULL DEFAULT 0,
    total_nuevos       INT NOT NULL DEFAULT 0,
    total_duplicados   INT NOT NULL DEFAULT 0,
    total_errores      INT NOT NULL DEFAULT 0,
    usuario_id         UUID REFERENCES usuarios (id),
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    confirmada_at      TIMESTAMP
);
CREATE INDEX idx_importaciones_empresa ON importaciones (empresa_id, created_at DESC);

CREATE TABLE importacion_registros (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    importacion_id        UUID NOT NULL REFERENCES importaciones (id) ON DELETE CASCADE,
    hoja_origen           VARCHAR(100),
    fila_origen           INT,
    nombre_original       VARCHAR(255) NOT NULL,
    nombre_normalizado    VARCHAR(255),
    turno                 VARCHAR(20),
    centro_costo          VARCHAR(150),
    cargo                 VARCHAR(150),
    telefono              VARCHAR(50),
    direccion             TEXT,
    comuna                VARCHAR(100),
    -- Detectado por anotaciones tipo "No utiliza el servicio" / "ocasional".
    uso_transporte_detectado VARCHAR(20)
                       CHECK (uso_transporte_detectado IN ('SI', 'NO', 'OCASIONAL', 'PENDIENTE')),
    tipo_match            VARCHAR(20) NOT NULL
                       CHECK (tipo_match IN ('EXACTO', 'TOKENS', 'SUGERENCIA', 'NUEVO', 'DUPLICADO', 'ERROR')),
    pasajero_id           UUID REFERENCES pasajeros (id),
    candidatos_json       TEXT,
    resolucion            VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                       CHECK (resolucion IN ('PENDIENTE', 'ACEPTADO', 'NUEVO', 'DESCARTADO')),
    mensaje_error         TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_import_registros_importacion ON importacion_registros (importacion_id);
