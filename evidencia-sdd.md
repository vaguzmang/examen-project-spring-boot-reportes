# Evidencia SDD y TDD — LogiTrack IQ

## 1. Documentación SDD

El desarrollo fue definido antes de la implementación mediante los siguientes documentos:

- [01 - Propuesta](docs/sdd/01-propuesta.md)
- [02 - Especificación](docs/sdd/02-especificacion.md)
- [03 - Diseño](docs/sdd/03-diseno.md)
- [04 - Tareas](docs/sdd/04-tareas.md)

## 2. Commits obligatorios

Los tres commits requeridos fueron realizados en el orden solicitado:

1. `0a8932dedfa738bf85481ba1f3bc53de5c689bcc`  
   **docs: define LogiTrack IQ scope**

2. `b39b3883ff8228624ac787d380bb944582442f62`  
   **test: define reorder and order-state rules**

3. `6796ff1f916a9b1d61981b1e87e4363aa7981cc0`  
   **feat: implement LogiTrack IQ rules**

## 3. Evidencia TDD

### RED

Antes de implementar las reglas de negocio se crearon pruebas que fallaban porque los contratos todavía no existían.

- [Evidencia RED](docs/sdd/evidence/red-tests.txt)
- [Pruebas TDD](src/test/java/com/project/springboot/demoproject/logitrack/LogiTrackIqRedTests.java)

### GREEN

Después de implementar las reglas y agregar pruebas de integración se ejecutó la suite completa.

Resultado final:

- **19 pruebas ejecutadas**
- **0 fallos**
- **0 errores**
- **BUILD SUCCESS**
- Base de pruebas: **H2 en memoria**, independiente de MySQL/Docker.

- [Evidencia GREEN](docs/sdd/evidence/green-tests.txt)
- [Pruebas de integración](src/test/java/com/project/springboot/demoproject/logitrack/LogiTrackIqIntegrationTests.java)

## 4. Trazabilidad regla → prueba

| Regla requerida | Prueba / evidencia |
|---|---|
| Consumo 0 → `diasCobertura = null` | `consumoCeroDebeGenerarCoberturaNula` |
| Consumo 0 → `SIN_CONSUMO` | `consumoCeroDebeGenerarEstadoSinConsumo` |
| Stock igual al punto de reorden no está en riesgo | `stockIgualPuntoReordenNoDebeEstarEnRiesgo` |
| Cantidad de orden `<= 0` → HTTP 400 | `cantidadCeroONegativaEnOrdenDebeResponder400` |
| CANCELADA → APROBADA → HTTP 400 | `ordenCanceladaNoPuedeVolverAAprobada` |
| APROBADA → RECIBIDA crea ENTRADA | `aprobadaARecibidaDebeCrearMovimientoEntrada` |
| AGENTE no puede aprobar → HTTP 403 | `agenteIntentandoAprobarOrdenDebeResponder403` |
| Severidad inválida → HTTP 400 y conserva resumen anterior | `panelConSeveridadInvalidaDebeResponder400YConservarAnterior` |
| ID inexistente → HTTP 400 y conserva resumen anterior | `panelConIdInexistenteDebeResponder400YConservarAnterior` |
| PDF BORRADOR se almacena y contiene marca BORRADOR | `pdfBorradorDebeGuardarseTenerMarcaYEliminarseAlCambiarEstado` |
| Cambio de estado invalida PDF anterior | `pdfBorradorDebeGuardarseTenerMarcaYEliminarseAlCambiarEstado` |
| PATCH de estado rechaza campos adicionales | `patchEstadoConCampoExtraDebeResponder400` |
| Integración real del endpoint PATCH estado | `LogiTrackIqIntegrationTests` |

## 5. Reflexión

El uso de SDD permitió definir primero las reglas, límites de seguridad, estados y arquitectura antes de modificar el backend existente. Con TDD se fijaron los comportamientos críticos como contratos verificables antes de implementar la lógica. La etapa RED demostró que esas reglas todavía no existían y la etapa GREEN confirmó su implementación. Posteriormente se añadieron pruebas de integración para verificar no solo funciones aisladas, sino respuestas HTTP, seguridad por roles, persistencia, generación de PDF y recepción transaccional de órdenes. Separar las pruebas con H2 también permitió que la suite fuera reproducible sin depender de una instancia local de MySQL.
