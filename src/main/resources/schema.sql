DROP TABLE IF EXISTS auditoria;
DROP TABLE IF EXISTS resumen_panel;
DROP TABLE IF EXISTS orden_compra;
DROP TABLE IF EXISTS movimiento_detalle;
DROP TABLE IF EXISTS movimiento;
DROP TABLE IF EXISTS inventario_bodega;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS proveedor;
DROP TABLE IF EXISTS bodega;
DROP TABLE IF EXISTS usuario;
CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    rol VARCHAR(20) NOT NULL DEFAULT 'EMPLEADO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bodega (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(150) NOT NULL,
    capacidad INTEGER NOT NULL CHECK (capacidad > 0),
    encargado VARCHAR(100) NOT NULL
);

CREATE TABLE proveedor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    contacto VARCHAR(200),
    dias_entrega INTEGER NOT NULL
        CHECK (dias_entrega BETWEEN 1 AND 90)
);

CREATE TABLE producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    precio NUMERIC(12,2) NOT NULL CHECK (precio >= 0),
    proveedor_principal_id BIGINT
        REFERENCES proveedor(id) ON DELETE SET NULL
);

CREATE TABLE inventario_bodega (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bodega_id BIGINT NOT NULL
        REFERENCES bodega(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL
        REFERENCES producto(id) ON DELETE CASCADE,
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    CONSTRAINT uk_inventario_bodega_producto
        UNIQUE (bodega_id, producto_id)
);

CREATE INDEX idx_inventario_stock_bajo
ON inventario_bodega(stock);

CREATE TABLE movimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo VARCHAR(20) NOT NULL,
    usuario_id BIGINT NOT NULL
        REFERENCES usuario(id) ON DELETE RESTRICT,
    bodega_origen_id BIGINT
        REFERENCES bodega(id) ON DELETE RESTRICT,
    bodega_destino_id BIGINT
        REFERENCES bodega(id) ON DELETE RESTRICT,

    CONSTRAINT chk_movimiento_bodegas CHECK (
        (
            tipo='ENTRADA'
            AND bodega_origen_id IS NULL
            AND bodega_destino_id IS NOT NULL
        )
        OR
        (
            tipo='SALIDA'
            AND bodega_origen_id IS NOT NULL
            AND bodega_destino_id IS NULL
        )
        OR
        (
            tipo='TRANSFERENCIA'
            AND bodega_origen_id IS NOT NULL
            AND bodega_destino_id IS NOT NULL
            AND bodega_origen_id<>bodega_destino_id
        )
    )
);

CREATE INDEX idx_movimiento_fecha
ON movimiento(fecha);

CREATE INDEX idx_movimiento_usuario
ON movimiento(usuario_id);

CREATE TABLE movimiento_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movimiento_id BIGINT NOT NULL
        REFERENCES movimiento(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL
        REFERENCES producto(id) ON DELETE RESTRICT,
    cantidad INTEGER NOT NULL CHECK(cantidad > 0)
);

CREATE INDEX idx_detalle_movimiento
ON movimiento_detalle(movimiento_id);

CREATE INDEX idx_detalle_producto
ON movimiento_detalle(producto_id);

CREATE TABLE orden_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL
        REFERENCES producto(id) ON DELETE RESTRICT,
    proveedor_id BIGINT NOT NULL
        REFERENCES proveedor(id) ON DELETE RESTRICT,
    bodega_destino_id BIGINT NOT NULL
        REFERENCES bodega(id) ON DELETE RESTRICT,
    cantidad INTEGER NOT NULL CHECK(cantidad > 0),
    precio_unitario NUMERIC(14,2) NOT NULL
        CHECK(precio_unitario > 0),
    total NUMERIC(16,2) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL
        CHECK (
            estado IN (
                'BORRADOR',
                'APROBADA',
                'RECIBIDA',
                'CANCELADA'
            )
        ),
    creado_por BIGINT NOT NULL
        REFERENCES usuario(id) ON DELETE RESTRICT,
    pdf LONGBLOB,
    fecha_generacion_pdf TIMESTAMP
);

CREATE INDEX idx_orden_estado
ON orden_compra(estado);

CREATE TABLE resumen_panel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL UNIQUE,
    contenido_json TEXT NOT NULL,
    autor_id BIGINT NOT NULL
        REFERENCES usuario(id) ON DELETE RESTRICT
);

CREATE TABLE auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_operacion VARCHAR(20) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL
        REFERENCES usuario(id) ON DELETE RESTRICT,
    entidad_afectada VARCHAR(100) NOT NULL,
    entidad_id BIGINT,
    producto_id BIGINT,
    campo_modificado VARCHAR(100),
    valores_anteriores TEXT,
    valores_nuevos TEXT
);

CREATE INDEX idx_auditoria_usuario
ON auditoria(usuario_id);

CREATE INDEX idx_auditoria_tipo
ON auditoria(tipo_operacion);

CREATE INDEX idx_auditoria_entidad
ON auditoria(entidad_afectada);

CREATE INDEX idx_auditoria_fecha
ON auditoria(fecha_hora);

CREATE INDEX idx_auditoria_producto
ON auditoria(producto_id);
