# MCP Server — LogiTrack IQ

Servidor MCP que expone **exactamente seis herramientas** y consume únicamente
la API REST de Spring Boot autenticándose como `AGENTE`.

## Arquitectura

`n8n → MCP → API Spring Boot → MySQL`

El MCP no accede directamente a MySQL y no contiene reglas de negocio.

## Variables

```bash
LOGITRACK_API_URL=http://localhost:8080
AGENTE_USERNAME=agente
AGENTE_PASSWORD=<configurar en .env, no versionar>
MCP_HOST=127.0.0.1
MCP_PORT=3001
```

## Ejecución

```bash
cd mcp-server
npm install
npm start
```

Healthcheck: `GET http://127.0.0.1:3001/health`

MCP: `http://127.0.0.1:3001/mcp`

## Herramientas

| # | Herramienta | API REST |
|---|---|---|
| 1 | `consultar_stock_producto(productoId)` | `GET /productos/{id}/stock` |
| 2 | `consultar_bodegas_criticas()` | `GET /bodegas/criticas` |
| 3 | `consultar_productos_en_riesgo()` | `GET /productos/riesgo` |
| 4 | `consultar_kpis()` | `GET /kpis` |
| 5 | `crear_orden_borrador(...)` | `POST /ordenes` |
| 6 | `publicar_resumen(resumen)` | `POST /panel/resumen` |

No existe herramienta para aprobar, cancelar ni recibir órdenes.

Ver evidencia: [`evidence/demo-tools.md`](evidence/demo-tools.md).
