# Diseño — LogiTrack IQ

## Arquitectura

n8n
  |
  v
MCP
  |
  v
Spring Boot REST API
  |
  v
Base de datos
  |
  v
Dashboard

La lógica de negocio permanece en Spring Boot.

## Componentes existentes reutilizados
- Producto
- Bodega
- Movimiento
- MovimientoDetalle
- Usuario
- JWT
- Seguridad
- Auditoría
- Manejo global de excepciones

## Nuevos componentes

### Entidades
- Proveedor
- OrdenCompra
- ResumenPanel

### Enums
- EstadoOrdenCompra
- EstadoCobertura
- SeveridadAlerta
- TipoAccionResumen
- Rol.AGENTE

### Servicios

InventarioCalculoService:
- stock por producto/bodega
- stock total
- consumo diario
- punto de reorden
- cobertura
- bodega sugerida
- ocupación
- bodegas críticas
- movimientos de ayer

OrdenCompraService:
- crear BORRADOR
- consultar órdenes
- cambiar estado
- recibir orden transaccionalmente
- invalidar PDF

KpiService:
- compone KPIs.

ResumenPanelService:
- valida y publica resumen.

OrdenPdfService:
- genera y almacena PDF.

## Flujo principal

Movimientos
-> cálculo stock
-> producto en riesgo
-> n8n consulta mediante MCP
-> orden BORRADOR
-> ADMIN aprueba
-> ADMIN recibe
-> movimiento ENTRADA
-> stock actualizado
-> dashboard actualizado

## Persistencia
El proyecto actual utiliza PostgreSQL.
El entregable final debe alinearse con MySQL.
La nueva lógica debe evitar nuevas dependencias específicas de PostgreSQL.


## Estado final de persistencia

El backend heredado partía de PostgreSQL. La implementación entregada quedó
migrada y reproducible sobre **MySQL 8.0**.

Diagrama final:

`n8n → MCP → API Spring Boot → MySQL → dashboard`
