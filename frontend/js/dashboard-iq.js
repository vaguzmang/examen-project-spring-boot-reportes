/*
 * ============================================================
 * LOGITRACK IQ - TORRE DE CONTROL
 * ============================================================
 *
 * Extiende el dashboard original sin reemplazar su navegación,
 * estilos ni módulos existentes.
 *
 * Fuente única de verdad:
 * API REST Spring Boot.
 */

async function mostrarInicio() {

    moduloCargadoActualmente = "inicio";
    marcarModuloActivo("inicio");

    document.getElementById("pageTitle").textContent =
        "Torre de control";

    const contenedor =
        document.getElementById("mainContent");

    contenedor.innerHTML = `
        <div class="loading-state">
            <div>
                <div class="spinner"></div>
                Cargando Torre de Control...
            </div>
        </div>
    `;

    try {

        /*
         * El resumen de IA puede no existir todavía en una
         * instalación nueva. Por eso se consulta con allSettled.
         */
        const resultados = await Promise.allSettled([
            peticionApi(API.RUTAS.KPIS),
            peticionApi(API.RUTAS.PRODUCTOS_RIESGO),
            peticionApi(API.RUTAS.BODEGAS_CRITICAS),
            peticionApi(API.RUTAS.PANEL_RESUMEN),
            peticionApi(API.RUTAS.ORDENES + "?estado=BORRADOR")
        ]);

        const kpis =
            resultadoObligatorio(resultados[0], "KPIs");

        const riesgos =
            resultadoOpcional(resultados[1], []);

        const bodegasCriticas =
            resultadoOpcional(resultados[2], []);

        const resumenIa =
            resultadoOpcional(resultados[3], null);

        const ordenesBorrador =
            resultadoOpcional(resultados[4], []);


        if (!esModuloVigente("inicio")) {
            return;
        }


        contenedor.innerHTML = `
            ${cabeceraTorreControl()}

            ${renderizarKpisIq(kpis)}

            ${renderizarMovimientosAyer(
                kpis.movimientosAyer || {}
            )}

            <div class="dashboard-grid fade-in">

                ${renderizarOcupacionBodegas(
                    kpis.ocupacionBodegas || []
                )}

                ${renderizarResumenIa(resumenIa)}

            </div>

            ${renderizarBodegasCriticas(
                bodegasCriticas
            )}

            ${renderizarProductosRiesgo(
                riesgos
            )}

            ${renderizarOrdenesBorrador(
                ordenesBorrador
            )}
        `;

    } catch (error) {

        if (!esModuloVigente("inicio")) {
            return;
        }

        contenedor.innerHTML = `
            <div class="panel empty-state">
                <strong>
                    No fue posible cargar la Torre de Control
                </strong>
                <span>
                    ${escaparHtml(error.message)}
                </span>
                <br>
                <button
                    class="btn btn-primary"
                    onclick="mostrarInicio()">
                    Reintentar
                </button>
            </div>
        `;
    }
}


/* ============================================================
   RESULTADOS API
   ============================================================ */

function resultadoObligatorio(resultado, nombre) {

    if (resultado.status === "fulfilled") {
        return resultado.value;
    }

    throw new Error(
        `No fue posible consultar ${nombre}: ` +
        resultado.reason.message
    );
}


function resultadoOpcional(resultado, valorDefecto) {

    return resultado.status === "fulfilled"
        ? resultado.value
        : valorDefecto;
}


/* ============================================================
   CABECERA
   ============================================================ */

function cabeceraTorreControl() {

    const ahora = new Intl.DateTimeFormat(
        "es-CO",
        {
            dateStyle: "long",
            timeStyle: "short",
            timeZone: "America/Bogota"
        }
    ).format(new Date());

    return `
        <div class="page-head fade-in">
            <div>
                <h2>LogiTrack IQ</h2>
                <p>
                    Estado operativo del inventario,
                    abastecimiento y alertas inteligentes.
                </p>
                <small style="
                    color:var(--text-muted,#6b7280);
                    display:block;
                    margin-top:.35rem">
                    Actualizado: ${escaparHtml(ahora)}
                </small>
            </div>

            <button
                class="btn btn-secondary"
                onclick="mostrarInicio()">
                ↻ Actualizar
            </button>
        </div>
    `;
}


/* ============================================================
   KPIs
   ============================================================ */

function renderizarKpisIq(kpis) {

    return `
        <div class="kpi-grid fade-in">

            ${tarjetaKpi(
                "!",
                "Productos en quiebre",
                formatoNumero(
                    kpis.productosQuiebre || 0
                ),
                "stock total en cero",
                "#fee2e2",
                "#dc2626"
            )}

            ${tarjetaKpi(
                "△",
                "Productos en riesgo",
                formatoNumero(
                    kpis.productosRiesgo || 0
                ),
                "bajo punto de reorden",
                "#fef3c7",
                "#d97706"
            )}

            ${tarjetaKpi(
                "▤",
                "Órdenes BORRADOR",
                formatoNumero(
                    kpis.ordenesBorrador || 0
                ),
                "pendientes de revisión",
                "#dbeafe",
                "#2563eb"
            )}

            ${tarjetaKpi(
                "$",
                "Valor BORRADOR",
                formatoMoneda(
                    kpis.totalOrdenesBorrador || 0
                ),
                "valor total pendiente",
                "#dcfce7",
                "#16a34a"
            )}

        </div>
    `;
}


/* ============================================================
   MOVIMIENTOS DE AYER
   ============================================================ */

function renderizarMovimientosAyer(movimientos) {

    const entradas =
        Number(movimientos.ENTRADA || 0);

    const salidas =
        Number(movimientos.SALIDA || 0);

    const transferencias =
        Number(movimientos.TRANSFERENCIA || 0);

    return `
        <article class="panel fade-in"
                 style="margin-bottom:1rem">

            <div class="panel-head">
                <div>
                    <h3>Movimientos de ayer</h3>
                    <span>
                        Actividad registrada en inventario
                    </span>
                </div>
            </div>

            <div class="kpi-grid">

                ${tarjetaKpi(
                    "↓",
                    "Entradas",
                    formatoNumero(entradas),
                    "movimientos",
                    "#dcfce7",
                    "#16a34a"
                )}

                ${tarjetaKpi(
                    "↑",
                    "Salidas",
                    formatoNumero(salidas),
                    "movimientos",
                    "#fee2e2",
                    "#dc2626"
                )}

                ${tarjetaKpi(
                    "⇄",
                    "Transferencias",
                    formatoNumero(transferencias),
                    "movimientos",
                    "#dbeafe",
                    "#2563eb"
                )}

            </div>
        </article>
    `;
}


/* ============================================================
   OCUPACIÓN DE BODEGAS
   ============================================================ */

function renderizarOcupacionBodegas(lista) {

    if (!Array.isArray(lista) || lista.length === 0) {

        return `
            <article class="panel">
                <div class="panel-head">
                    <h3>Ocupación de bodegas</h3>
                </div>

                <div class="empty-state">
                    No hay bodegas disponibles.
                </div>
            </article>
        `;
    }


    const filas = lista.map(item => {

        const porcentaje =
            Number(item.ocupacion || 0);

        const ancho =
            Math.min(Math.max(porcentaje, 0), 100);

        const estado =
            porcentaje >= 90
                ? "Crítica"
                : porcentaje >= 75
                    ? "Alta"
                    : "Normal";

        const clase =
            porcentaje >= 90
                ? "badge-red"
                : porcentaje >= 75
                    ? "badge-amber"
                    : "badge-green";

        return `
            <div class="warehouse-row">

                <div style="min-width:130px">
                    <strong>
                        ${escaparHtml(
                            item.nombreBodega || ""
                        )}
                    </strong>

                    <small style="
                        display:block;
                        color:var(--text-muted,#6b7280)">
                        ${formatoNumero(item.unidades || 0)}
                        /
                        ${formatoNumero(item.capacidad || 0)}
                        unidades
                    </small>
                </div>

                <div class="bar-track">
                    <span
                        class="bar-fill"
                        style="width:${ancho}%">
                    </span>
                </div>

                <div style="
                    min-width:90px;
                    text-align:right">

                    <strong>
                        ${porcentaje.toFixed(2)}%
                    </strong>

                    <br>

                    <span class="badge ${clase}">
                        ${estado}
                    </span>

                </div>

            </div>
        `;
    }).join("");


    return `
        <article class="panel">

            <div class="panel-head">
                <div>
                    <h3>Ocupación de bodegas</h3>
                    <span>
                        Capacidad utilizada actualmente
                    </span>
                </div>
            </div>

            <div class="warehouse-bars">
                ${filas}
            </div>

        </article>
    `;
}


/* ============================================================
   RESUMEN IA / N8N
   ============================================================ */

function renderizarResumenIa(resumen) {

    if (!resumen) {

        return `
            <article class="panel">

                <div class="panel-head">
                    <div>
                        <h3>Resumen inteligente</h3>
                        <span>
                            Automatización n8n + MCP
                        </span>
                    </div>
                </div>

                <div class="empty-state">
                    Aún no se ha publicado un resumen diario.
                </div>

            </article>
        `;
    }


    const alertas =
        Array.isArray(resumen.alertas)
            ? resumen.alertas
            : [];

    const acciones =
        Array.isArray(resumen.accionesSugeridas)
            ? resumen.accionesSugeridas
            : [];


    return `
        <article class="panel">

            <div class="panel-head">
                <div>
                    <h3>Resumen inteligente</h3>
                    <span>
                        ${escaparHtml(
                            resumen.fecha || ""
                        )}
                        · n8n + MCP
                    </span>
                </div>
            </div>

            <p style="
                line-height:1.65;
                margin-bottom:1rem">
                ${escaparHtml(
                    resumen.narrativa || ""
                )}
            </p>

            <div style="margin-bottom:1rem">

                <strong>
                    Alertas
                </strong>

                <div style="
                    display:grid;
                    gap:.5rem;
                    margin-top:.6rem">

                    ${
                        alertas.length
                            ? alertas
                                .map(renderizarAlertaIa)
                                .join("")
                            : `
                                <span style="
                                    color:var(--text-muted,#6b7280)">
                                    Sin alertas.
                                </span>
                              `
                    }

                </div>

            </div>

            <div>

                <strong>
                    Acciones sugeridas
                </strong>

                <div style="
                    display:grid;
                    gap:.5rem;
                    margin-top:.6rem">

                    ${
                        acciones.length
                            ? acciones
                                .map(renderizarAccionIa)
                                .join("")
                            : `
                                <span style="
                                    color:var(--text-muted,#6b7280)">
                                    Sin acciones sugeridas.
                                </span>
                              `
                    }

                </div>

            </div>

        </article>
    `;
}


function renderizarAlertaIa(alerta) {

    const severidad =
        alerta.severidad || "MEDIA";

    const clase =
        severidad === "ALTA"
            ? "badge-red"
            : severidad === "MEDIA"
                ? "badge-amber"
                : "badge-green";

    return `
        <div style="
            display:flex;
            gap:.65rem;
            align-items:flex-start">

            <span class="badge ${clase}">
                ${escaparHtml(severidad)}
            </span>

            <span>
                <strong>
                    ${escaparHtml(
                        alerta.titulo || "Alerta"
                    )}
                </strong>
                <br>
                ${escaparHtml(
                    alerta.detalle ||
                    alerta.mensaje ||
                    ""
                )}
            </span>

        </div>
    `;
}


function renderizarAccionIa(accion) {

    return `
        <div style="
            display:flex;
            gap:.65rem;
            align-items:flex-start">

            <span class="badge badge-blue">
                ${escaparHtml(
                    accion.tipo || ""
                )}
            </span>

            <span>
                ${escaparHtml(
                    accion.descripcion || ""
                )}
            </span>

        </div>
    `;
}


/* ============================================================
   BODEGAS CRÍTICAS
   ============================================================ */

function renderizarBodegasCriticas(lista) {

    if (!Array.isArray(lista) || lista.length === 0) {

        return `
            <article class="panel fade-in"
                     style="margin-top:1rem">

                <div class="panel-head">
                    <div>
                        <h3>Bodegas críticas</h3>
                        <span>Ocupación igual o superior al 90%</span>
                    </div>
                </div>

                <div class="empty-state">
                    No existen bodegas en nivel crítico.
                </div>

            </article>
        `;
    }


    const filas = lista.map(item => `
        <tr>
            <td>#${item.bodegaId}</td>

            <td class="cell-title">
                ${escaparHtml(item.nombreBodega)}
            </td>

            <td>
                ${formatoNumero(item.unidades)}
            </td>

            <td>
                ${formatoNumero(item.capacidad)}
            </td>

            <td>
                <span class="badge badge-red">
                    ${Number(item.ocupacion).toFixed(2)}%
                </span>
            </td>
        </tr>
    `).join("");


    return `
        <article class="panel fade-in"
                 style="margin-top:1rem">

            <div class="panel-head">
                <div>
                    <h3>Bodegas críticas</h3>
                    <span>
                        Requieren atención operativa
                    </span>
                </div>
            </div>

            <div class="table-scroll">

                <table>

                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Bodega</th>
                            <th>Unidades</th>
                            <th>Capacidad</th>
                            <th>Ocupación</th>
                        </tr>
                    </thead>

                    <tbody>
                        ${filas}
                    </tbody>

                </table>

            </div>

        </article>
    `;
}


/* ============================================================
   PRODUCTOS EN RIESGO
   ============================================================ */

function renderizarProductosRiesgo(lista) {

    if (!Array.isArray(lista) || lista.length === 0) {

        return `
            <article class="panel fade-in"
                     style="margin-top:1rem">

                <div class="panel-head">
                    <div>
                        <h3>Productos en riesgo</h3>
                        <span>
                            Punto de reorden y cobertura
                        </span>
                    </div>
                </div>

                <div class="empty-state">
                    No existen productos bajo el punto de reorden.
                </div>

            </article>
        `;
    }


    const filas = lista.map(item => {

        const cobertura =
            item.diasCobertura == null
                ? "—"
                : Number(
                    item.diasCobertura
                ).toFixed(1) + " días";

        return `
            <tr>

                <td>
                    #${item.productoId}
                </td>

                <td class="cell-title">
                    ${escaparHtml(
                        item.nombreProducto
                    )}
                </td>

                <td>
                    ${formatoNumero(
                        item.stockTotal
                    )}
                </td>

                <td>
                    ${Number(
                        item.consumoDiarioPromedio || 0
                    ).toFixed(2)}
                </td>

                <td>
                    ${Number(
                        item.puntoReorden || 0
                    ).toFixed(2)}
                </td>

                <td>
                    ${cobertura}
                </td>

                <td>
                    <span class="badge badge-amber">
                        ${escaparHtml(
                            item.estadoCobertura || ""
                        )}
                    </span>
                </td>

                <td>
                    Bodega #${item.bodegaDestinoId}
                </td>

            </tr>
        `;
    }).join("");


    return `
        <article class="panel fade-in"
                 style="margin-top:1rem">

            <div class="panel-head">
                <div>
                    <h3>Productos en riesgo</h3>
                    <span>
                        Productos que requieren evaluación
                        de abastecimiento
                    </span>
                </div>
            </div>

            <div class="table-scroll">

                <table>

                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Producto</th>
                            <th>Stock</th>
                            <th>Consumo/día</th>
                            <th>Punto reorden</th>
                            <th>Cobertura</th>
                            <th>Estado</th>
                            <th>Destino sugerido</th>
                        </tr>
                    </thead>

                    <tbody>
                        ${filas}
                    </tbody>

                </table>

            </div>

        </article>
    `;
}


/* ============================================================
   ÓRDENES BORRADOR
   ============================================================ */

function renderizarOrdenesBorrador(lista) {

    if (!Array.isArray(lista) || lista.length === 0) {

        return `
            <article class="panel fade-in"
                     style="margin-top:1rem">

                <div class="panel-head">
                    <div>
                        <h3>Órdenes BORRADOR</h3>
                        <span>
                            Órdenes pendientes de revisión administrativa
                        </span>
                    </div>
                </div>

                <div class="empty-state">
                    No existen órdenes BORRADOR.
                </div>

            </article>
        `;
    }


    const filas = lista.map(item => `
        <tr>

            <td>
                #${item.id}
            </td>

            <td class="cell-title">
                ${escaparHtml(
                    item.nombreProducto || ""
                )}
            </td>

            <td>
                ${escaparHtml(
                    item.nombreProveedor || ""
                )}
            </td>

            <td>
                ${escaparHtml(
                    item.nombreBodegaDestino || ""
                )}
            </td>

            <td>
                ${formatoNumero(
                    item.cantidad || 0
                )}
            </td>

            <td>
                ${formatoMoneda(
                    item.precioUnitario || 0
                )}
            </td>

            <td>
                <strong>
                    ${formatoMoneda(
                        item.total || 0
                    )}
                </strong>
            </td>

            <td>
                <span class="badge badge-blue">
                    ${escaparHtml(item.estado)}
                </span>
            </td>

        </tr>
    `).join("");


    return `
        <article class="panel fade-in"
                 style="margin-top:1rem">

            <div class="panel-head">
                <div>
                    <h3>Órdenes BORRADOR</h3>
                    <span>
                        Pendientes de revisión administrativa
                    </span>
                </div>
            </div>

            <div class="table-scroll">

                <table>

                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Producto</th>
                            <th>Proveedor</th>
                            <th>Destino</th>
                            <th>Cantidad</th>
                            <th>Precio unitario</th>
                            <th>Total</th>
                            <th>Estado</th>
                        </tr>
                    </thead>

                    <tbody>
                        ${filas}
                    </tbody>

                </table>

            </div>

        </article>
    `;
}
