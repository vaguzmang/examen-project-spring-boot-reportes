# LogiTrack IQ — Sistema inteligente de gestión de inventario

**LogiTrack IQ** es una extensión de la plataforma LogiTrack que agrega una **torre de control inteligente de inventario** capaz de calcular automáticamente el stock real desde movimientos, detectar productos en riesgo, gestionar órdenes de compra y publicar indicadores operacionales. Incluye integración con **MCP (Model Context Protocol)** e **n8n** para automación de procesos.

---

## Arquitectura General

```
┌─────────────────┐
│  n8n Workflow   │  (Orquestación diaria)
│  06:00 AM       │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│  MCP Server (Model Context Protocol - 6 herramientas)  │
│                                                         │
│  1. consultar_stock_producto(productoId)                │
│  2. consultar_bodegas_criticas()                        │
│  3. consultar_productos_en_riesgo()                     │
│  4. consultar_kpis()                                    │
│  5. crear_orden_borrador(...)                           │
│  6. publicar_resumen(...)                               │
│                                                         │
│  Consumo: API REST únicamente (sin acceso directo a BD) │
└────────┬────────────────────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────────────┐
│  API REST — Spring Boot 3.3 (Java 17)            │
│                                                  │
│  • Endpoints LogiTrack IQ                        │
│  • Seguridad JWT + Roles (ADMIN, AGENTE)         │
│  • Auditoría automática                          │
│  • Cálculo de stock desde movimientos            │
│  • Gestión de órdenes de compra                  │
│  • Panel de resumen inteligente                  │
└────────┬───────────────────────────────────────┘
         │
         ▼
┌──────────────────────────┐
│  MySQL 8.0               │
│  (schema + data)         │
│  Timezone: America/Bogota│
└──────────────────────────┘
```

### Consumidores de la API REST:

- **Dashboard HTML/CSS/JS**: interfaz web sin framework, en `src/main/resources/static/`
- **MCP Server**: Node.js, en `mcp-server/` — orquesta operaciones mediante 6 herramientas
- **n8n Workflow**: ejecuta automáticamente cada día a las 06:00 (America/Bogota)

**Nota**: Frontend, MCP y n8n **nunca acceden directamente a MySQL**. Toda operación pasa por la API REST.

---

## 1. Descripción General

LogiTrack IQ añade inteligencia al inventario:

- **Stock calculado desde movimientos**: ENTRADA suma, SALIDA resta, TRANSFERENCIA afecta origen y destino
- **Detección de riesgo**: productos cuyo stock cae por debajo del punto de reorden
- **Gestión de órdenes**: ciclo BORRADOR → APROBADA → RECIBIDA, con PDF y auditoría
- **KPIs en tiempo real**: productos en quiebre, en riesgo, órdenes pendientes, ocupación de bodegas
- **Panel inteligente**: resumen diario con alertas y acciones sugeridas
- **Automatización**: workflow n8n ejecuta análisis diario y crea órdenes si es necesario
- **Integración MCP**: 6 herramientas para consultas y operaciones programáticas

---

## 2. Tecnologías

| Componente | Tecnología |
|---|---|
| **Lenguaje / Runtime** | Java 17 |
| **Framework Backend** | Spring Boot 3.3.4 (Web, Data JPA, Security, Validation) |
| **Base de datos** | MySQL 8.0 (no PostgreSQL) |
| **Seguridad** | Spring Security + JWT (JJWT 0.12.6) |
| **API REST** | Spring Boot Web + Jackson |
| **Documentación** | springdoc-openapi (Swagger/OpenAPI) |
| **Frontend** | HTML5 / CSS3 / JavaScript puro (sin frameworks) |
| **Build** | Maven (empaquetado en `.jar`) |
| **Orquestación** | n8n (workflow automation) |
| **Integración** | MCP Server (Node.js) |
| **Containerización** | Docker + docker-compose |
| **Timezone** | America/Bogota (global) |

---

## 3. Estructura del Proyecto

```
.
├── Dockerfile                              # Docker image para Spring Boot
├── docker-compose.yml                      # Orquestación de MySQL + Spring Boot
├── .env.example                            # Variables de entorno (placeholders)
│
├── pom.xml                                 # Configuración Maven
├── mvnw / mvnw.cmd                         # Maven wrapper
│
├── src/
│   ├── main/
│   │   ├── java/com/project/springboot/demoproject/
│   │   │   ├── controllers/                # Endpoints REST
│   │   │   │   ├── LogiTrackInventarioController      # /kpis, /productos/riesgo, /bodegas/criticas
│   │   │   │   ├── OrdenCompraController              # /ordenes (CRUD + estado + PDF)
│   │   │   │   ├── ResumenPanelController             # POST/GET /panel/resumen
│   │   │   │   └── ... (otros módulos)
│   │   │   │
│   │   │   ├── services/
│   │   │   │   ├── InventarioCalculoService           # Stock desde movimientos
│   │   │   │   ├── InventarioReglas                   # Lógica: riesgo, reorden, cobertura
│   │   │   │   ├── OrdenCompraService                 # Órdenes y transiciones
│   │   │   │   ├── OrdenCompraReglas                  # Validación de estados
│   │   │   │   ├── KpiService                         # Indicadores
│   │   │   │   ├── ResumenPanelService                # Panel inteligente
│   │   │   │   ├── ResumenPanelReglas                 # Validación de resumen
│   │   │   │   ├── OrdenPdfService                    # Generación de PDF
│   │   │   │   └── ... (otros servicios)
│   │   │   │
│   │   │   ├── entities/
│   │   │   │   ├── Producto                           # +proveedorPrincipal (ManyToOne)
│   │   │   │   ├── Proveedor                          # diasEntrega (1-90)
│   │   │   │   ├── OrdenCompra                        # Estados: BORRADOR, APROBADA, RECIBIDA, CANCELADA
│   │   │   │   ├── ResumenPanel                       # Resumen diario
│   │   │   │   ├── Movimiento / MovimientoDetalle     # Fuente de verdad para stock
│   │   │   │   └── ... (otras entidades)
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── ProductoRiesgoResponse
│   │   │   │   ├── StockProductoResponse
│   │   │   │   ├── BodegaCriticaResponse
│   │   │   │   ├── KpiResponse
│   │   │   │   ├── ResumenPanelRequest
│   │   │   │   ├── OrdenCompraRequest / OrdenCompraResponse
│   │   │   │   └── ... (otros DTOs)
│   │   │   │
│   │   │   ├── enums/
│   │   │   │   ├── Rol                                # SUPERADMIN, ADMIN, EMPLEADO, AGENTE
│   │   │   │   ├── EstadoOrdenCompra                 # BORRADOR, APROBADA, RECIBIDA, CANCELADA
│   │   │   │   ├── TipoMovimiento                     # ENTRADA, SALIDA, TRANSFERENCIA
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── security/                  # JWT, SecurityConfig, UserDetailsService
│   │   │   ├── audit/                     # EntityListeners, auditoría automática
│   │   │   ├── exception/                 # GlobalExceptionHandler, ErrorResponse
│   │   │   ├── config/                    # OpenAPI / Swagger
│   │   │   └── repositories/              # Spring Data JPA
│   │   │
│   │   └── resources/
│   │       ├── application.properties      # Config principal (MySQL, JWT, timezone)
│   │       ├── application-test.properties # Config para tests (H2 in-memory)
│   │       ├── schema.sql                  # DDL (tabla de producto con proveedorPrincipal, etc.)
│   │       ├── data.sql                    # Data de prueba (usuarios, bodegas, productos, proveedores)
│   │       └── static/                     # Frontend HTML/CSS/JS
│   │           ├── index.html
│   │           ├── login.html
│   │           ├── dashboard.html
│   │           ├── css/  js/  pages/
│   │           └── ... (modulos: ordenes, inventario, kpis, etc.)
│   │
│   └── test/
│       ├── java/com/project/springboot/demoproject/logitrack/
│       │   ├── LogiTrackIqRedTests        # TDD RED phase (9 pruebas)
│       │   └── LogiTrackIqIntegrationTests # Integración (9 pruebas)
│       └── resources/
│           └── application-test.properties
│
├── frontend/                               # Fuente alternativa del frontend (desarrollo)
│   ├── index.html  login.html  dashboard.html
│   ├── css/  js/  pages/
│   └── README_FRONTEND.md
│
├── mcp-server/                             # MCP Server (Node.js)
│   ├── src/
│   │   └── index.js                       # 6 herramientas MCP
│   ├── package.json
│   └── README.md
│
├── n8n/
│   └── Resumen_diario_de_inventario.json  # Workflow: 06:00 AM diario
│
├── skills/
│   └── operacion-logitrack/
│       └── SKILL.md                       # Guía de uso del MCP server
│
├── docs/
│   ├── sdd/
│   │   ├── 01-propuesta.md
│   │   ├── 02-especificacion.md
│   │   ├── 03-diseno.md
│   │   ├── 04-tareas.md
│   │   └── evidence/
│   │       ├── red-tests.txt
│   │       └── green-tests.txt
│   └── capturas/
│       └── (screenshots de demostración)
│
├── evidencia-sdd.md                       # Commits obligatorios + trazabilidad
└── README.md                              # Este archivo
```

---

## 4. Requisitos

### Locales (desarrollo sin Docker)
- **JDK 17** o superior
- **Maven 3.8+** (o usar `./mvnw` incluido)
- **MySQL 8.0+** corriendo en `localhost:3307`
- Usuario MySQL: `logitrack` (configurar contraseña propia)

### Con Docker
- **Docker** (últimas versiones)
- **docker-compose** (v1.29+)

No hace falta tener MySQL instalado localmente; docker-compose lo levanta automáticamente.

---

## 5. Ejecución Local (sin Docker)

### 5.1 Preparar la base de datos

```bash
# Conectar a MySQL como root
mysql -u root -p

# Crear base de datos (solo una vez)
CREATE DATABASE logitrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Crear usuario
CREATE USER 'logitrack'@'localhost' IDENTIFIED BY 'tu_contraseña_segura';
GRANT ALL PRIVILEGES ON logitrack.* TO 'logitrack'@'localhost';
FLUSH PRIVILEGES;
```

### 5.2 Configurar aplicación

Edita `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/logitrack?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=utf-8
spring.datasource.username=logitrack
spring.datasource.password=tu_contraseña_segura
spring.sql.init.mode=always
```

O usa variables de entorno (recomendado):

```bash
export DB_URL="jdbc:mysql://localhost:3307/logitrack?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=utf-8"
export DB_USERNAME=logitrack
export DB_PASSWORD=tu_contraseña_segura
export JWT_SECRET="generar_una_clave_aleatoria_larga_y_segura"
export JWT_EXPIRATION_MS=86400000
```

### 5.3 Ejecutar

```bash
# Opción 1: compilar y ejecutar JAR
./mvnw clean package -DskipTests
java -jar target/demoproject-0.0.1-SNAPSHOT.jar

# Opción 2: modo desarrollo
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

### 5.4 Acceso inicial

- **URL**: `http://localhost:8080/`
- **Swagger**: `http://localhost:8080/swagger-ui.html`
- **Dashboard**: `http://localhost:8080/dashboard.html`

**Usuarios de prueba** (creados automáticamente por `data.sql` para desarrollo/demostración):

| Username | Password (test only) | Rol |
|---|---|---|
| superadmin | superadmin123 | SUPERADMIN |
| admin | admin123 | ADMIN |
| jperez | empleado123 | EMPLEADO |
| agente | (set in .env) | AGENTE |

⚠️ **Cambiar contraseñas en producción**. Los usuarios de prueba son solo para desarrollo local.

---

## 6. Ejecución con Docker (Recomendado)

### 6.1 Preparar variables de entorno

```bash
# Copiar y editar .env.example
cp .env.example .env

# Si en Linux/Mac, asegurar permisos
chmod 600 .env
```

No commits `.env` a Git. El archivo está en `.gitignore`.

### 6.2 Construir e iniciar

```bash
# Construir imagen y levantar servicios
docker-compose up --build

# En background
docker-compose up -d --build

# Ver logs
docker-compose logs -f spring-boot
docker-compose logs -f mysql
```

### 6.3 Esperar a que esté listo

Los servicios incluyen healthchecks:

- **MySQL**: ping cada 10 segundos (retries: 5)
- **Spring Boot**: curl a `/swagger-ui.html` cada 30 segundos (retries: 3)

Spring Boot **espera a que MySQL esté saludable** antes de iniciar.

Esperado ~30-60 segundos en el primer inicio (mientras se descargan imágenes base).

### 6.4 Acceder

- API: `http://localhost:8080`
- Dashboard: `http://localhost:8080/dashboard.html`
- Swagger: `http://localhost:8080/swagger-ui.html`
- MySQL: `localhost:3307` (usuario: `logitrack`, contraseña: ver `.env`)

### 6.5 Detener

```bash
# Parar sin eliminar volúmenes (datos persisten)
docker-compose down

# Parar y limpiar (elimina volúmenes ⚠️ destructivo)
docker-compose down -v

# Ver volúmenes
docker volume ls
```

---

## 7. Base de Datos

### Diseño

**Fuente de verdad para stock**: tabla `movimiento_detalle`

- **ENTRADA**: suma en `bodega_destino`
- **SALIDA**: resta en `bodega_origen`
- **TRANSFERENCIA**: resta en origen, suma en destino
- **Stock negativo**: no permitido (validación en negocio)

**Tablas principales**:
- `usuario` — SUPERADMIN, ADMIN, EMPLEADO, AGENTE
- `bodega` — ubicación, capacidad, encargado
- `producto` — nombre, categoría, precio, **proveedor_principal_id**
- `proveedor` — nombre, contacto, **dias_entrega** (1-90)
- `movimiento` / `movimiento_detalle` — ENTRADA/SALIDA/TRANSFERENCIA
- `orden_compra` — BORRADOR, APROBADA, RECIBIDA, CANCELADA
- `resumen_panel` — resumen diario inteligente
- `auditoria` — registro automático de cambios

### Inicialización

Spring Boot ejecuta automáticamente:
1. `schema.sql` — crea tablas si no existen
2. `data.sql` — inserta datos de prueba

```properties
spring.sql.init.mode=always
```

### Timezone

Base de datos y aplicación están configuradas para **America/Bogota**:

```properties
spring.datasource.url=...&serverTimezone=America/Bogota&characterEncoding=utf-8
spring.jpa.properties.hibernate.jdbc.time_zone=America/Bogota
```

---

## 8. Seguridad

### Autenticación

**JWT (JSON Web Token)**:
- Expira en 24 horas (configurable: `JWT_EXPIRATION_MS`)
- Se guarda en `sessionStorage` del navegador (nunca localStorage)
- Se envía en header: `Authorization: Bearer <token>`

### Roles y Permisos

| Rol | Descripción | Permisos |
|---|---|---|
| **SUPERADMIN** | Administrador total | Todos (heredades de ADMIN) + crear ADMIN |
| **ADMIN** | Gestor de inventario | Bodegas, productos, órdenes, auditoría, crear EMPLEADO |
| **EMPLEADO** | Operador diario | Movimientos, consultas, ver auditoría |
| **AGENTE** | Operador automático (MCP/n8n) | Consultar KPIs, riesgo, crear BORRADOR, publicar resumen |

### Restricciones LogiTrack IQ

- **AGENTE NO puede**: aprobar órdenes, recibir órdenes, cancelar órdenes, crear movimientos manuales
- **Cambio de estado** de orden: solo ADMIN/SUPERADMIN
- **Aprobación de orden**: crea automáticamente movimiento ENTRADA en la misma transacción
- **PDF de orden**: se elimina al cambiar estado (invalida PDF anterior)

### Auditoría

Cada cambio en entidades auditables se registra automáticamente:
- Usuario responsable
- Tipo de operación (INSERT, UPDATE, DELETE)
- Entidad afectada
- Valores antes y después

Visible en dashboard módulo **Auditoría**.

---

## 9. Endpoints LogiTrack IQ

### Consultas (GET)

#### KPIs
```
GET /kpis
```
Retorna indicadores principales:
- `productosQuiebre` — stock total == 0
- `productosRiesgo` — stock < punto de reorden
- `ordenesBorrador` — cantidad de órdenes BORRADOR
- `totalOrdenesBorrador` — suma de totales
- `movimientosAyer` — movimientos del día anterior por tipo
- `ocupacionBodegas` — % de ocupación por bodega

**Roles permitidos**: AGENTE, ADMIN

#### Stock de Producto
```
GET /productos/{id}/stock
```
Retorna stock calculado desde movimientos:
- Stock por bodega
- Stock total

**Roles permitidos**: AGENTE, ADMIN

#### Productos en Riesgo
```
GET /productos/riesgo
```
Retorna productos cuyo stock < punto de reorden:
- `productoId`, `nombre`
- `stockTotal`, `puntoReorden`, `consumoDiarioPromedio`
- `diasCobertura`, `estadoCobertura`
- `proveedorId`, `proveedorNombre`, `diasEntrega`
- `bodegaDestinoId` — donde crear orden de compra

**Nota**: solo incluye productos con `proveedor_principal_id` NO NULL

**Roles permitidos**: AGENTE, ADMIN

#### Bodegas Críticas
```
GET /bodegas/criticas
```
Retorna bodegas con ocupación >= 90%:
- `bodegaId`, `nombre`
- `unidades`, `capacidad`, `ocupacionPorcentaje`

**Roles permitidos**: AGENTE, ADMIN

#### Proveedores
```
GET /proveedores
```
Listado completo de proveedores.

**Roles permitidos**: AGENTE, ADMIN

### Órdenes de Compra

#### Listar Órdenes
```
GET /ordenes?estado=BORRADOR
```
Listado filtrable por estado.

**Roles permitidos**: AGENTE, ADMIN

#### Obtener Orden
```
GET /ordenes/{id}
```

#### Crear Orden BORRADOR
```
POST /ordenes
Content-Type: application/json

{
  "productoId": 1,
  "proveedorId": 1,
  "bodegaDestinoId": 1,
  "cantidad": 50,
  "precioUnitario": 2500000.00
}
```

**Validaciones**:
- `cantidad > 0` (HTTP 400 si no)
- Total se calcula: `precioUnitario * cantidad`
- Se crea en estado BORRADOR

**Roles permitidos**: AGENTE, ADMIN

#### Cambiar Estado de Orden
```
PATCH /ordenes/{id}/estado
Content-Type: application/json

{
  "estado": "APROBADA"
}
```

**Transiciones permitidas**:
- BORRADOR → APROBADA | CANCELADA
- APROBADA → RECIBIDA | CANCELADA
- RECIBIDA → (terminal)
- CANCELADA → (terminal)

**APROBADA → RECIBIDA**: crea automáticamente movimiento ENTRADA en la misma transacción

**Validaciones**:
- Transición inválida → HTTP 400
- AGENTE intenta cambiar estado → HTTP 403
- Solo campo `estado` permitido (campos extras → HTTP 400)

**Roles permitidos**: ADMIN (AGENTE no puede)

#### Generar PDF de Orden
```
POST /ordenes/{id}/pdf
```

Respuesta: `Content-Type: application/pdf`

- Se almacena en la BD (`pdf` BLOB)
- Se registra `fecha_generacion_pdf`
- Si estado es BORRADOR: marca de agua diagonal semitransparente "BORRADOR"

**Roles permitidos**: AGENTE, ADMIN

#### Obtener PDF
```
GET /ordenes/{id}/pdf
```

- Si no existe → HTTP 404
- Respuesta: `Content-Type: application/pdf`

### Panel de Resumen

#### Publicar Resumen
```
POST /panel/resumen
Content-Type: application/json

{
  "fecha": "2026-08-30",
  "narrativa": "Resumen operativo del día con análisis de KPIs y alertas. Texto entre 20 y 500 caracteres.",
  "alertas": [
    {
      "mensaje": "Producto bajo stock",
      "severidad": "ALTA",
      "productoId": 1,
      "ordenId": null,
      "bodegaId": null
    }
  ],
  "accionesSugeridas": [
    {
      "tipo": "REVISAR_PRODUCTO",
      "descripcion": "Revisar cantidad de unidades",
      "productoId": 1,
      "ordenId": null,
      "bodegaId": null
    }
  ]
}
```

**Validaciones**:
- `fecha` = fecha actual de America/Bogota
- `narrativa`: 20-500 caracteres
- `severidad` ∈ {BAJA, MEDIA, ALTA}
- `tipo` ∈ {REVISAR_ORDEN, REVISAR_PRODUCTO, REVISAR_BODEGA}
- Cada alerta: al menos un ID válido (productoId, ordenId, bodegaId)
- Cada acción: exactamente un ID válido
- Si resumen inválido → HTTP 400 y se conserva resumen anterior del día
- Solo un resumen válido por fecha

**Roles permitidos**: AGENTE, ADMIN

#### Obtener Resumen
```
GET /panel/resumen
```

Retorna el último resumen válido (si existe), o HTTP 400.

---

## 10. Órdenes de Compra — Ciclo de Vida

1. **BORRADOR** (inicial)
   - Creada por usuario/MCP
   - PDF puede generarse (con marca BORRADOR)
   - Puede cambiar a APROBADA o CANCELADA

2. **APROBADA**
   - Cambio desde BORRADOR
   - PDF anterior se invalida
   - Puede cambiar a RECIBIDA o CANCELADA

3. **RECIBIDA**
   - Cambio desde APROBADA (crea ENTRADA automáticamente)
   - PDF anterior se invalida
   - Estado terminal (no más cambios)
   - Marca recepción: stock se actualiza por ENTRADA

4. **CANCELADA**
   - Cambio desde BORRADOR o APROBADA
   - PDF anterior se invalida
   - Estado terminal (no más cambios)

---

## 11. PDF de Órdenes

### Generación
```
POST /ordenes/{id}/pdf
```

- Genera documento PDF
- Se almacena en base de datos (campo `pdf` BLOB)
- Se registra `fecha_generacion_pdf`
- Si estado BORRADOR: incluye marca "BORRADOR" (diagonal, semitransparente)

### Visualización
```
GET /ordenes/{id}/pdf
```

- Retorna `Content-Type: application/pdf`
- HTTP 404 si no existe

### Invalidación
- Al cambiar estado de la orden, PDF se elimina
- No se puede acceder a PDF de orden anterior
- Al volver a generar PDF, se recrea (sin marca si ya no es BORRADOR)

---

## 12. Dashboard / Frontend

Frontend sin framework (HTML/CSS/JavaScript puro) ubicado en **`frontend/`** (principal) y replicado en `src/main/resources/static/` para servicio desde el backend.

### Módulos
- **Dashboard Principal**: resumen de KPIs, últimos movimientos
- **Ordenes**: CRUD con cambios de estado, PDF
- **Inventario**: stock por producto y bodega
- **Movimientos**: registro de ENTRADA/SALIDA/TRANSFERENCIA
- **Bodegas**: gestión
- **Productos**: gestión + proveedor principal
- **Proveedores**: gestión
- **Auditoría**: historial completo de cambios
- **Usuarios**: gestión de roles (solo ADMIN/SUPERADMIN)
- **Panel Resumen**: publicación y visualización

### Ejecución

**Opción 1: Servido por Spring Boot** (recomendado para producción)
```
http://localhost:8080/
http://localhost:8080/dashboard.html
```

**Opción 2: Ejecución independiente con Live Server** (desarrollo)
- Abre la carpeta `frontend/` en VS Code
- Instala extensión "Live Server" de Ritwick Dey
- Haz clic derecho en `index.html` → "Open with Live Server"
- Frontend ejecutándose en `http://localhost:5500`
- Configura API en `frontend/js/api.js` si es necesario

**Autenticación**: JWT en sessionStorage, nunca en localStorage

**Control de acceso**: botones ocultados según rol; backend valida permisos

---

## 13. MCP Server

### Ubicación
`mcp-server/` — Node.js

### Herramientas (EXACTAMENTE 6)

1. **consultar_stock_producto(productoId)**
   - Consulta stock real del producto
   - Incluye precio (usado para crear órdenes)

2. **consultar_bodegas_criticas()**
   - Listado de bodegas con ocupación >= 90%

3. **consultar_productos_en_riesgo()**
   - Productos cuyo stock < punto de reorden
   - Incluye datos para crear orden (proveedor, bodega destino)

4. **consultar_kpis()**
   - Indicadores principales del sistema

5. **crear_orden_borrador(productoId, proveedorId, bodegaDestinoId, cantidad, precioUnitario)**
   - Crea orden en estado BORRADOR
   - Cantidad debe ser > 0
   - No aprueba automáticamente

6. **publicar_resumen(resumen)**
   - Publica resumen diario
   - Valida estructura completa

### Configuración

Variables de entorno:

```bash
LOGITRACK_API_URL=http://localhost:8080        # URL de API
AGENTE_USERNAME=agente                         # Usuario AGENTE
AGENTE_PASSWORD=tu_contrasena_segura           # Contraseña (desde .env)
MCP_HOST=127.0.0.1                             # Host MCP
MCP_PORT=3001                                  # Puerto MCP
```

### Autenticación

MCP se autentica contra la API REST:
1. LOGIN con credenciales de AGENTE
2. Obtiene JWT
3. Todas las llamadas incluyen JWT en headers
4. Si JWT expira, reintenta login automáticamente

### Ejecutar

```bash
cd mcp-server
npm install
LOGITRACK_API_URL=http://localhost:8080 \
AGENTE_USERNAME=agente \
AGENTE_PASSWORD=tu_contrasena_segura \
npm start
```

MCP Server escucha en `127.0.0.1:3001` (configurable)

---

## 14. n8n Workflow

### Archivo
`n8n/Resumen_diario_de_inventario.json`

### Programación
- **Trigger**: Schedule cada día a las **06:00 AM** (America/Bogota)
- **Ejecución manual**: también disponible para demostración

### Pasos

1. **Consultar KPIs** — `consultar_kpis()`
2. **Consultar Productos en Riesgo** — `consultar_productos_en_riesgo()`
3. **Consultar Bodegas Críticas** — `consultar_bodegas_criticas()`
4. Si hay riesgo:
   - Seleccionar primer producto
   - `consultar_stock_producto(productoId)` — obtener precio real
   - Calcular cantidad: `ceil(max(1, puntoReorden * 2 - stockTotal))`
   - `crear_orden_borrador(...)` — máximo UNA orden por ejecución
5. **Publicar Resumen** — `publicar_resumen(...)` con datos reales

### Restricciones
- **Nunca** aprueba órdenes
- **Nunca** cancela órdenes
- **Nunca** recibe órdenes
- **Nunca** accede directamente a MySQL
- **Nunca** inventa datos (si una herramienta falla, reporta error)
- Máximo **1 orden BORRADOR** por ejecución

### Instalación

```bash
# Copiar JSON en n8n UI:
# 1. Abrir n8n
# 2. Menu > Import workflow
# 3. Pegar contenido de Resumen_diario_de_inventario.json
# 4. Configurar credenciales MCP (URL, usuario, contraseña)
# 5. Activar workflow
```

---

## 15. Skill: operacion-logitrack

### Ubicación
`skills/operacion-logitrack/SKILL.md`

### Propósito
Guía de uso y restricciones para el agente de n8n que orquesta la operación diaria.

### Contenido
- Secuencia obligatoria de herramientas
- Prohibiciones explícitas
- Fórmula de cantidad de orden
- Validación de resumen
- Manejo de errores

---

## 16. SDD y TDD

### Documentación SDD

- [01 - Propuesta](docs/sdd/01-propuesta.md)
- [02 - Especificación](docs/sdd/02-especificacion.md)
- [03 - Diseño](docs/sdd/03-diseno.md)
- [04 - Tareas](docs/sdd/04-tareas.md)

### Commits Obligatorios

```
0a8932d docs: define LogiTrack IQ scope
b39b388 test: define reorder and order-state rules
6796ff1 feat: implement LogiTrack IQ rules
```

(Ver [evidencia-sdd.md](evidencia-sdd.md))

### Test-Driven Development

**RED phase**: pruebas sin implementación
- Archivo: [LogiTrackIqRedTests.java](src/test/java/com/project/springboot/demoproject/logitrack/LogiTrackIqRedTests.java)
- Resultado: [red-tests.txt](docs/sdd/evidence/red-tests.txt)

**GREEN phase**: implementación + pruebas de integración
- Archivo: [LogiTrackIqIntegrationTests.java](src/test/java/com/project/springboot/demoproject/logitrack/LogiTrackIqIntegrationTests.java)
- Resultado: [green-tests.txt](docs/sdd/evidence/green-tests.txt)

---

## 17. Suite de Tests

### Ejecución

```bash
./mvnw clean test
```

### Resultados

```
Tests run: 19
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### Cobertura de Requisitos

| Requisito | Test | Estado |
|---|---|---|
| Consumo 0 → cobertura null | consumoCeroDebeGenerarCoberturaNula | ✓ |
| Consumo 0 → SIN_CONSUMO | consumoCeroDebeGenerarEstadoSinConsumo | ✓ |
| stock == puntoReorden → NO riesgo | stockIgualPuntoReordenNoDebeEstarEnRiesgo | ✓ |
| cantidad <= 0 → 400 | cantidadCeroONegativaEnOrdenDebeResponder400 | ✓ |
| CANCELADA no aprobable → 400 | ordenCanceladaNoPuedeVolverAAprobada | ✓ |
| APROBADA→RECIBIDA crea ENTRADA | aprobadaARecibidaDebeCrearMovimientoEntrada | ✓ |
| AGENTE aprobando → 403 | agenteIntentandoAprobarOrdenDebeResponder403 | ✓ |
| Severidad inválida → 400 conserva | panelConSeveridadInvalidaDebeResponder400YConservarAnterior | ✓ |
| ID inexistente → 400 conserva | panelConIdInexistenteDebeResponder400YConservarAnterior | ✓ |
| PDF BORRADOR marca + elimina | pdfBorradorDebeGuardarseTenerMarcaYEliminarseAlCambiarEstado | ✓ |
| PATCH rechaza campos extras | patchEstadoConCampoExtraDebeResponder400 | ✓ |

### Base de Tests

- **Tecnología**: JUnit 5 + Spring Boot Test + Mockito
- **Base de datos**: H2 en memoria (reproducible, sin dependencias externas)
- **Profiles**: `@ActiveProfiles("test")`

---

## 18. Swagger / OpenAPI

### Acceso

- **UI**: `http://localhost:8080/swagger-ui.html`
- **JSON**: `http://localhost:8080/v3/api-docs`

### Autenticación en Swagger

1. Hacer login en `/auth/login` (botón POST)
2. Copiar el `token` de la respuesta
3. Click en botón **Authorize** (arriba a la derecha)
4. Pegar: `Bearer <token>`
5. Click en **Authorize**

Todos los endpoints protegidos estarán disponibles para probar.

### Endpoints Documentados

Todos los endpoints LogiTrack IQ incluyen:
- Descripción
- Parámetros
- Modelo de respuesta
- Códigos HTTP
- Requerimiento de autenticación Bearer JWT

---

## 19. Configuración

### application.properties

Configuración de producción (MySQL local):

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/logitrack?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=utf-8
spring.datasource.username=logitrack
spring.datasource.password=${DB_PASSWORD}
spring.jpa.properties.hibernate.jdbc.time_zone=America/Bogota
jwt.secret=${JWT_SECRET}
jwt.expiration-ms=${JWT_EXPIRATION_MS}
```

**Nota**: Usar variables de entorno en lugar de valores hardcodeados (ver sección 5.2 y 6.1).

### application-test.properties

Configuración para tests (H2 en memoria):

```properties
spring.datasource.url=jdbc:h2:mem:logitracktest;MODE=MySQL
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.properties.hibernate.jdbc.time_zone=America/Bogota
```

### Variables de Entorno

Recomendadas (sobre propiedades hardcoded):

```bash
DB_URL=...
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=...
JWT_EXPIRATION_MS=...
SQL_INIT_MODE=always
```

Usando Docker: configuradas automáticamente en `docker-compose.yml`.

---

## 20. Troubleshooting

### "No database selected"
- Ejecutar `data.sql` en MySQL manualmente: `USE logitrack; source src/main/resources/data.sql;`
- O recrear BD: `DROP DATABASE logitrack; CREATE DATABASE logitrack;` y reiniciar Spring Boot

### "Timezone offset mismatch"
- Confirmar: `SELECT @@global.time_zone, @@session.time_zone;` en MySQL (debe ser `+00:00`)
- Confirmar JVM: `System.getProperty("user.timezone")` debe ser `America/Bogota`

### "JWT expired"
- Token tiene TTL de 24h (`JWT_EXPIRATION_MS=86400000`)
- Hacer login de nuevo

### "AGENTE no puede aprobar"
- Esto es correcto. Solo ADMIN/SUPERADMIN pueden cambiar estado de órdenes.
- MCP y agente crean borradores; ADMIN aprueba

### Tests no pasan
```bash
# Limpiar y recompillar
./mvnw clean compile test

# Si sigue fallando, verificar MySQL está disponible (para integración local)
# O pasar -DskipTests para ignorar
./mvnw clean package -DskipTests
```

### Docker: "Cannot connect to Docker daemon"
```bash
# Verificar que Docker está corriendo
docker ps

# En Linux, posible necesidad de permisos
sudo usermod -aG docker $USER
newgrp docker
```

---

## 21. Información Adicional

### Timezone: America/Bogota

Aplicada globalmente:
- JDBC: `serverTimezone=America/Bogota`
- Hibernate: `hibernate.jdbc.time_zone=America/Bogota`
- Inicializadores de entidades: `LocalDateTime.now(ZoneId.of("America/Bogota"))`
- n8n: configurado en workflow
- MySQL (Docker): `TZ=America/Bogota`

### No PostgreSQL

El proyecto usa **MySQL 8.0**, no PostgreSQL. Asegurar variable `DB_URL` apunta a MySQL.

### Credenciales en Git

- `.env` real está en `.gitignore` (NO se versionan)
- `.env.example` tiene placeholders (se versionan como referencia)
- `application.properties` tiene valores por defecto seguros (NO tienen secretos reales)

### Reproducibilidad

Docker + docker-compose asegura que el proyecto es reproducible en cualquier máquina con Docker:
- Sin dependencias de configuración local
- Base de datos incluida
- Volúmenes para persistencia
- Healthchecks para robustez

---

## 22. Support y Contacto

Para problemas, consultar:
1. [evidencia-sdd.md](evidencia-sdd.md) — trazabilidad de requisitos
2. `docs/sdd/` — documentación de diseño
3. Swagger UI — testing de endpoints
4. Git history — cambios y intención

---

**Última actualización**: 2026-08-30
**Versión**: 1.0
**Estatus**: Entrega completa — LogiTrack IQ
