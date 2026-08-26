# Especificación — LogiTrack IQ

## Inventario
Zona horaria: America/Bogota.

El stock se calcula a partir de movimientos y sus detalles.

- ENTRADA suma en bodega destino.
- SALIDA resta en bodega origen.
- TRANSFERENCIA resta en origen y suma en destino.
- No se permite stock negativo.
- Stock total = suma del stock calculado en todas las bodegas.

## Proveedor
Campos:
- id
- nombre
- contacto
- diasEntrega

diasEntrega debe estar entre 1 y 90.

## Producto
Agregar proveedorPrincipal como relación ManyToOne opcional.

Producto sin proveedor principal no aparece como producto en riesgo.

## OrdenCompra
Campos:
- id
- producto
- proveedor
- bodegaDestino
- cantidad
- precioUnitario
- total
- fechaCreacion
- estado
- creadoPor
- pdf
- fechaGeneracionPdf

Cantidad > 0.
Total calculado por servidor.

## ResumenPanel
Campos:
- id
- fecha
- contenidoJson
- autor

Solo un resumen válido por fecha.

## Cálculos
Ocupación:
(unidades almacenadas / capacidad) * 100

Productos en quiebre:
stockTotal == 0

Productos en riesgo:
proveedorPrincipal != null y stockTotal < puntoReorden

Consumo diario:
SALIDAS últimos 30 días / 30

Punto de reorden:
consumoDiarioPromedio * diasEntrega * 1.5

Cobertura:
stockTotal / consumoDiarioPromedio

Si consumo == 0:
diasCobertura = null
estadoCobertura = SIN_CONSUMO

Si stockTotal == puntoReorden, NO está en riesgo.

Bodega crítica:
ocupación >= 90%

## Estados
BORRADOR -> APROBADA o CANCELADA
APROBADA -> RECIBIDA o CANCELADA
RECIBIDA -> ninguno
CANCELADA -> ninguno

Transición inválida: 400.

APROBADA -> RECIBIDA crea movimiento ENTRADA en la misma transacción.

## API
GET /kpis
GET /productos/{id}/stock
GET /productos/riesgo
GET /bodegas/criticas
GET /proveedores
GET /ordenes
POST /ordenes
GET /ordenes/{id}
POST /ordenes/{id}/pdf
GET /ordenes/{id}/pdf
PATCH /ordenes/{id}/estado
POST /panel/resumen
GET /panel/resumen

## Seguridad
Agregar rol AGENTE.

AGENTE puede consultar KPIs, stock, riesgos, bodegas críticas, crear BORRADOR y publicar resumen.

AGENTE no puede aprobar, recibir, cancelar ni registrar movimientos manualmente.

## MCP
Exactamente seis herramientas:
1. consultar_stock_producto
2. consultar_bodegas_criticas
3. consultar_productos_en_riesgo
4. consultar_kpis
5. crear_orden_borrador
6. publicar_resumen

No existe herramienta para aprobar órdenes.
