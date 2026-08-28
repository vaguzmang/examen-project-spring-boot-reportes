# Skill: Operación LogiTrack IQ

## Propósito
Operar la torre de control de inventario LogiTrack IQ exclusivamente mediante las herramientas del servidor MCP. La API Spring Boot es la única fuente de verdad.

## Herramientas disponibles
El agente dispone únicamente de estas seis herramientas:
1. `consultar_stock_producto`
2. `consultar_bodegas_criticas`
3. `consultar_productos_en_riesgo`
4. `consultar_kpis`
5. `crear_orden_borrador`
6. `publicar_resumen`

No debe asumir herramientas adicionales.

## Secuencia obligatoria
En cada ejecución:
1. Consultar primero `consultar_kpis`.
2. Consultar después `consultar_productos_en_riesgo`.
3. Consultar `consultar_bodegas_criticas`.
4. Analizar los resultados.
5. Si hay productos en riesgo, considerar únicamente el primero.
6. Consultar `consultar_stock_producto(productoId)` para obtener el precio real del producto.
7. Usar ese valor real como `precioUnitario`; nunca inventar ni estimar precios.
8. Crear como máximo UNA orden de compra BORRADOR por ejecución.
9. Publicar un resumen diario con `publicar_resumen`.
10. Informar claramente el resultado o cualquier fallo.

## Regla de creación de orden
Solo puede crearse una orden si existe al menos un producto en riesgo.

Para el primer producto en riesgo:
`cantidad = ceil(max(1, puntoReorden * 2 - stockTotal))`

Usar los valores reales obtenidos de la API:
- `productoId`
- `proveedorId`
- `bodegaDestinoId`
- `cantidad`
- `precioUnitario`

Nunca inventar IDs ni valores. La orden debe permanecer en estado BORRADOR.

## Prohibiciones
El agente NO puede:
- aprobar órdenes;
- cancelar órdenes;
- recibir órdenes;
- crear movimientos manuales;
- acceder directamente a MySQL;
- modificar directamente la base de datos;
- inventar datos;
- crear más de una orden BORRADOR por ejecución;
- continuar silenciosamente si una herramienta falla.

Las operaciones APROBADA, RECIBIDA y CANCELADA pertenecen exclusivamente al ADMIN.

## Manejo de errores
Si una herramienta MCP falla:
1. No inventar resultados.
2. No asumir que la operación fue realizada.
3. Informar qué herramienta falló.
4. Incluir una descripción breve del error.
5. No ejecutar acciones posteriores que dependan del resultado fallido.
6. No publicar un resumen inválido o basado en datos inventados.

## Resumen del panel
El objeto enviado a `publicar_resumen` debe contener EXACTAMENTE estas propiedades de nivel superior:
- `fecha`
- `narrativa`
- `alertas`
- `accionesSugeridas`

La fecha debe ser la fecha actual en `America/Bogota`.
La narrativa debe tener entre 20 y 500 caracteres.
`alertas` y `accionesSugeridas` siempre deben ser arreglos.

Severidades válidas:
- `BAJA`
- `MEDIA`
- `ALTA`

Tipos de acción válidos:
- `REVISAR_ORDEN`
- `REVISAR_PRODUCTO`
- `REVISAR_BODEGA`

Cada alerta debe referenciar al menos un ID real.
Cada acción debe referenciar exactamente un ID real.

## Cuando no hay riesgo
Si `consultar_productos_en_riesgo` devuelve una lista vacía:
- no crear ninguna orden;
- publicar igualmente el resumen diario;
- indicar que no se detectaron productos bajo el punto de reorden;
- incluir alertas o acciones solo si los datos reales lo justifican.

## Arquitectura obligatoria
`n8n → MCP → API Spring Boot → MySQL`

Nunca:
`n8n → MySQL`
ni
`MCP → MySQL`
