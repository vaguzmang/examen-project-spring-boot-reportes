-- ==========================================================
-- LogiTrack IQ - datos reproducibles
-- America/Bogota
-- ==========================================================

INSERT INTO usuario
(username,password,email,rol,activo)
VALUES
(
 'superadmin',
 '$2b$10$0W/cZVLxlcjHCO8uQ9JBdOQVaZoZl1KT3FhFFfwRBL8lpawe0ct2C',
 'superadmin@logitrack.com',
 'SUPERADMIN',
 true
),
(
 'admin',
 '$2b$10$qQyp8JwcSeuSJ2QvXAtvp./0ue.HE1bd.PQHemQc/lgAm8zK6M9c.',
 'admin@logitrack.com',
 'ADMIN',
 true
),
(
 'jperez',
 '$2b$10$f.BSenCbRViwmSNylcXzQOUkyn.llXFHJ8q26pMlvQE6f39yVdaoK',
 'jperez@logitrack.com',
 'EMPLEADO',
 true
),
(
 'agente',
 '$2b$10$qQyp8JwcSeuSJ2QvXAtvp./0ue.HE1bd.PQHemQc/lgAm8zK6M9c.',
 'agente@logitrack.com',
 'AGENTE',
 true
);

INSERT INTO bodega
(nombre,ubicacion,capacidad,encargado)
VALUES
(
 'Bodega Central',
 'Bogota - Zona Industrial',
 500,
 'Carlos Gomez'
),
(
 'Bodega Norte',
 'Medellin - Guayabal',
 330,
 'Laura Restrepo'
),
(
 'Bodega Sur',
 'Cali - Yumbo',
 250,
 'Andres Torres'
);

INSERT INTO proveedor
(nombre,contacto,dias_entrega)
VALUES
(
 'Tecnologia Andina SAS',
 'compras@tecandina.com',
 10
),
(
 'Suministros Nacionales SAS',
 'ventas@suministros.com',
 7
),
(
 'Muebles Empresariales SAS',
 'pedidos@muebles.com',
 15
);

INSERT INTO producto
(nombre,categoria,precio,proveedor_principal_id)
VALUES
(
 'Laptop Lenovo ThinkPad E14',
 'Tecnologia',
 3200000,
 1
),
(
 'Mouse inalambrico Logitech',
 'Tecnologia',
 65000,
 1
),
(
 'Silla ergonomica oficina',
 'Mobiliario',
 450000,
 3
),
(
 'Escritorio en L',
 'Mobiliario',
 680000,
 3
),
(
 'Resma papel carta x500',
 'Papeleria',
 18000,
 2
),
(
 'Impresora multifuncional HP',
 'Tecnologia',
 890000,
 NULL
);

-- ==========================================================
-- INVENTARIO INICIAL REPRESENTADO POR MOVIMIENTOS ENTRADA
-- ==========================================================

INSERT INTO movimiento
(fecha,tipo,usuario_id,bodega_origen_id,bodega_destino_id)
VALUES
(
 DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 25 DAY),
 'ENTRADA',
 2,
 NULL,
 1
);

INSERT INTO movimiento_detalle
(movimiento_id,producto_id,cantidad)
VALUES
(1,1,85),
(1,2,120),
(1,3,8);

INSERT INTO movimiento
(fecha,tipo,usuario_id,bodega_origen_id,bodega_destino_id)
VALUES
(
 DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 DAY),
 'ENTRADA',
 2,
 NULL,
 2
);

INSERT INTO movimiento_detalle
(movimiento_id,producto_id,cantidad)
VALUES
(2,4,15),
(2,5,300);

INSERT INTO movimiento
(fecha,tipo,usuario_id,bodega_origen_id,bodega_destino_id)
VALUES
(
 DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 23 DAY),
 'ENTRADA',
 2,
 NULL,
 3
);

INSERT INTO movimiento_detalle
(movimiento_id,producto_id,cantidad)
VALUES
(3,6,5),
(3,2,40);

-- Consumo reciente:
-- Laptop: 60 unidades / 30 dias = 2 unidades/dia.
-- Punto reorden = 2 * 10 * 1.5 = 30.
-- Stock final = 25.
-- Resultado esperado: PRODUCTO EN RIESGO.

INSERT INTO movimiento
(fecha,tipo,usuario_id,bodega_origen_id,bodega_destino_id)
VALUES
(
 DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY),
 'SALIDA',
 2,
 1,
 NULL
);

INSERT INTO movimiento_detalle
(movimiento_id,producto_id,cantidad)
VALUES
(4,1,20);

INSERT INTO movimiento
(fecha,tipo,usuario_id,bodega_origen_id,bodega_destino_id)
VALUES
(
 DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY),
 'SALIDA',
 2,
 1,
 NULL
);

INSERT INTO movimiento_detalle
(movimiento_id,producto_id,cantidad)
VALUES
(5,1,20);

INSERT INTO movimiento
(fecha,tipo,usuario_id,bodega_origen_id,bodega_destino_id)
VALUES
(
 DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY),
 'SALIDA',
 2,
 1,
 NULL
);

INSERT INTO movimiento_detalle
(movimiento_id,producto_id,cantidad)
VALUES
(6,1,20);

-- Movimientos de ayer para KPI.

INSERT INTO movimiento
(fecha,tipo,usuario_id,bodega_origen_id,bodega_destino_id)
VALUES
(
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00:00'),
 'ENTRADA',
 2,
 NULL,
 1
);

INSERT INTO movimiento_detalle
(movimiento_id,producto_id,cantidad)
VALUES
(7,2,10);

INSERT INTO movimiento
(fecha,tipo,usuario_id,bodega_origen_id,bodega_destino_id)
VALUES
(
 TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '14:00:00'),
 'SALIDA',
 2,
 2,
 NULL
);

INSERT INTO movimiento_detalle
(movimiento_id,producto_id,cantidad)
VALUES
(8,5,5);

-- ==========================================================
-- Mirror del inventario legado.
-- LogiTrack IQ NO usa esta tabla como fuente de verdad.
-- ==========================================================

INSERT INTO inventario_bodega
(bodega_id,producto_id,stock)
VALUES
(1,1,25),
(1,2,130),
(1,3,8),
(2,4,15),
(2,5,295),
(3,6,5),
(3,2,40);
