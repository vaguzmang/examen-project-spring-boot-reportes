# Propuesta — LogiTrack IQ

## Problema
LogiTrack ya cuenta con un backend Spring Boot para bodegas, productos, usuarios, auditoría y movimientos, pero no posee una torre de control que calcule el inventario real desde movimientos, detecte riesgos de abastecimiento y gestione órdenes de compra.

## Objetivo
Extender el backend existente, sin reemplazarlo, para calcular inventario, detectar productos en riesgo, gestionar órdenes de compra, registrar su recepción, publicar indicadores y permitir integración mediante MCP y n8n.

## Alcance
- Mantener Spring Boot, Java 17, JWT, usuarios y auditoría existentes.
- Calcular stock desde movimientos.
- Agregar proveedores, órdenes de compra y resumen del panel.
- Implementar KPIs y productos en riesgo.
- Implementar estados BORRADOR, APROBADA, RECIBIDA y CANCELADA.
- Generar PDF de órdenes.
- Agregar rol AGENTE.
- Exponer API requerida.
- Implementar posteriormente MCP, n8n y dashboard HTML/CSS/JS.

## Fuera de alcance
- Crear otro backend.
- Reemplazar Spring Boot.
- React, Vue o Angular.
- Acceso directo de MCP o n8n a la base de datos.
- Herramienta MCP para aprobar órdenes.
