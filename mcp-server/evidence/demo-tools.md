# Evidencia de herramientas MCP — LogiTrack IQ

Evidencias visuales relacionadas:

- `docs/capturas/logitrack-iq/07_n8n_ejecucion_exitosa.png`
- `docs/capturas/logitrack-iq/08_n8n_error_controlado_mcp.png`
- `docs/capturas/logitrack-iq/09_pdf_orden_borrador.png`

## 1. consultar_stock_producto

Entrada:

```json
{"productoId":1}
```

Respuesta observada en el escenario de riesgo:

```json
{
  "productoId": 1,
  "nombreProducto": "Laptop Lenovo ThinkPad E14",
  "precio": 3200000,
  "stockTotal": 39
}
```

Desglose observado: Bodega Central 25, Bodega Norte 14 y Bodega Sur 0.

## 2. consultar_bodegas_criticas

Entrada:

```json
{}
```

Respuesta observada: Bodega Norte, 325/330 unidades, ocupación 98.48 %.

## 3. consultar_productos_en_riesgo

Entrada:

```json
{}
```

Respuesta observada:

```json
{
  "productoId": 1,
  "nombreProducto": "Laptop Lenovo ThinkPad E14",
  "proveedorId": 1,
  "stockTotal": 39,
  "consumoDiarioPromedio": 2.7,
  "puntoReorden": 40.5,
  "diasCobertura": 14.4444,
  "estadoCobertura": "CON_CONSUMO",
  "bodegaDestinoId": 3
}
```

## 4. consultar_kpis

Entrada:

```json
{}
```

La ejecución detectó un producto en riesgo y la Bodega Norte en condición
crítica. El contrato final expone `calculadoEn`, `ocupacionPorBodega`,
`productosEnQuiebre`, `productosEnRiesgo`, `ordenesPorAprobar` y
`movimientosAyer`.

## 5. crear_orden_borrador

Entrada:

```json
{
  "productoId": 1,
  "proveedorId": 1,
  "bodegaDestinoId": 3,
  "cantidad": 42,
  "precioUnitario": 3200000
}
```

Respuesta observada: orden `id=5`, cantidad 42, total 134400000, estado
`BORRADOR`, creada por `agente`.

## 6. publicar_resumen

Entrada contractual:

```json
{
  "resumen": {
    "fecha": "2026-08-30",
    "narrativa": "Se revisó el inventario; se detectó un producto en riesgo y se creó la orden borrador 5.",
    "alertas": [
      {
        "severidad": "ALTA",
        "titulo": "Producto en riesgo",
        "detalle": "Laptop Lenovo ThinkPad E14 está bajo su punto de reorden.",
        "productoId": 1
      }
    ],
    "accionesSugeridas": [
      {
        "tipo": "REVISAR_ORDEN",
        "descripcion": "Revisar la orden borrador 5.",
        "ordenId": 5
      }
    ]
  }
}
```

La ejecución exitosa publicó el resumen mostrado en el dashboard.

## Error controlado

Al detener temporalmente el MCP, n8n registró el fallo en el cliente MCP y el
agente. No hubo acceso directo a MySQL ni creación de una orden indebida.

Evidencia: `docs/capturas/logitrack-iq/08_n8n_error_controlado_mcp.png`.
