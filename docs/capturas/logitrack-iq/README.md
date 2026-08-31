# Evidencias visuales — LogiTrack IQ

Esta carpeta contiene las capturas solicitadas como evidencia funcional del proyecto integrador LogiTrack IQ.

## Evidencias

1. `01_dashboard_kpis.png` — Dashboard con KPIs principales.
2. `02_dashboard_resumen_inteligente.png` — Resumen generado mediante n8n + MCP, alertas y acciones sugeridas.
3. `03_dashboard_bodega_critica.png` — Bodega Norte identificada como crítica y estado del riesgo.
4. `04_dashboard_estado_final.png` — Estado final sin productos en riesgo ni órdenes BORRADOR.
5. `05_orden_5_recibida.png` — Orden de compra #5 en estado RECIBIDA, creada originalmente por AGENTE.
6. `06_movimiento_15_entrada.png` — Movimiento automático ENTRADA de 42 laptops hacia Bodega Sur.
7. `07_n8n_ejecucion_exitosa.png` — Ejecución satisfactoria del workflow con AI Agent y MCP.
8. `08_n8n_error_controlado_mcp.png` — Ejecución con error controlado al detener temporalmente MCP.
9. `09_pdf_orden_borrador.png` — PDF de la orden #5 con marca de agua BORRADOR.

## Flujo evidenciado

Riesgo de inventario → n8n → MCP → API Spring Boot → orden BORRADOR → PDF → aprobación administrativa → recepción → movimiento ENTRADA → actualización de stock.
