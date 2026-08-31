# Tareas — LogiTrack IQ

## SDD
- [x] Crear propuesta.
- [x] Crear especificación.
- [x] Crear diseño.
- [x] Crear plan de tareas.

## TDD RED
- [x] Prueba consumo 0 -> cobertura null y SIN_CONSUMO.
- [x] Prueba stock == puntoReorden -> no riesgo.
- [x] Prueba cantidad <= 0 -> 400.
- [x] Prueba CANCELADA -> APROBADA -> 400.
- [x] Prueba APROBADA -> RECIBIDA crea ENTRADA.
- [x] Prueba AGENTE intenta aprobar -> 403.
- [x] Prueba resumen inválido conserva anterior.
- [x] Prueba PDF BORRADOR.
- [x] Prueba integración PATCH estado o POST resumen.
- [x] Guardar evidencia RED.

## Backend
- [x] Crear Proveedor.
- [x] Agregar proveedorPrincipal a Producto.
- [x] Crear OrdenCompra.
- [x] Crear ResumenPanel.
- [x] Agregar AGENTE.
- [x] Implementar stock desde movimientos.
- [x] Implementar consumo diario.
- [x] Implementar punto de reorden.
- [x] Implementar cobertura.
- [x] Implementar KPIs.
- [x] Implementar riesgos.
- [x] Implementar bodegas críticas.
- [x] Implementar órdenes.
- [x] Implementar recepción transaccional.
- [x] Implementar PDF.
- [x] Implementar resumen.
- [x] Implementar seguridad.
- [x] Ejecutar GREEN.

## MCP
- [x] Crear servidor MCP.
- [x] Implementar exactamente seis herramientas.
- [x] Crear SKILL.md.

## n8n
- [x] Crear flujo Resumen diario de inventario.
- [x] Schedule 06:00 America/Bogota.
- [x] Integrar MCP.
- [x] Crear máximo una orden por ejecución.

## Frontend
- [x] HTML/CSS/JS vanilla.
- [x] KPIs.
- [x] riesgos.
- [x] órdenes BORRADOR.
- [x] PDF.
- [x] botón Aprobar solo ADMIN.

## MySQL
- [x] Migrar PostgreSQL a MySQL.
- [x] Eliminar tipos PostgreSQL específicos.
- [x] Verificar schema y data.sql.

## Entrega
- [x] evidencia-sdd.md
- [x] README.
- [x] Swagger.
- [x] Diagrama.
- [x] Video 4-6 minutos.
