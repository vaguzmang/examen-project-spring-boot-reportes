let ordenesActuales = [];
let ordenProductos = [];
let ordenProveedores = [];
let ordenBodegas = [];


/* ============================================================
   INICIAR
   ============================================================ */

async function iniciarOrdenes() {

    configurarCierreModal("orderModal");
    configurarCierreModal("orderDetailModal");

    const botonNuevo =
        document.getElementById("newOrderButton");

    botonNuevo.classList.toggle(
        "hidden",
        !puedeCrearOrden()
    );

    if (puedeCrearOrden()) {
        botonNuevo.addEventListener(
            "click",
            abrirOrdenNueva
        );
    }


    document
        .getElementById("orderForm")
        .addEventListener(
            "submit",
            guardarOrden
        );


    document
        .getElementById("orderSearch")
        .addEventListener(
            "input",
            filtrarOrdenes
        );


    document
        .getElementById("orderStatusFilter")
        .addEventListener(
            "change",
            cargarOrdenes
        );


    document
        .getElementById("orderProduct")
        .addEventListener(
            "change",
            actualizarDatosProductoOrden
        );


    try {

        const resultados =
            await Promise.all([
                peticionApi(API.RUTAS.PRODUCTOS),
                peticionApi(API.RUTAS.PROVEEDORES),
                peticionApi(API.RUTAS.BODEGAS)
            ]);


        ordenProductos = resultados[0];
        ordenProveedores = resultados[1];
        ordenBodegas = resultados[2];


        if (!esModuloVigente("orden")) {
            return;
        }


        llenarCatalogosOrden();

        await cargarOrdenes();

    } catch (error) {

        mostrarToast(
            error.message,
            "error"
        );
    }
}


function puedeCrearOrden() {

    return esAdmin() || esAgente();
}


/* ============================================================
   CATÁLOGOS
   ============================================================ */

function llenarCatalogosOrden() {

    const productos =
        document.getElementById("orderProduct");

    const proveedores =
        document.getElementById("orderSupplier");

    const bodegas =
        document.getElementById("orderWarehouse");


    productos.innerHTML =
        '<option value="">Selecciona un producto</option>' +
        ordenProductos
            .map(item => `
                <option value="${item.id}">
                    ${escaparHtml(item.nombre)}
                </option>
            `)
            .join("");


    proveedores.innerHTML =
        '<option value="">Selecciona un proveedor</option>' +
        ordenProveedores
            .map(item => `
                <option value="${item.id}">
                    ${escaparHtml(item.nombre)}
                </option>
            `)
            .join("");


    bodegas.innerHTML =
        '<option value="">Selecciona una bodega</option>' +
        ordenBodegas
            .map(item => `
                <option value="${item.id}">
                    ${escaparHtml(item.nombre)}
                </option>
            `)
            .join("");
}


/* ============================================================
   PRODUCTO SELECCIONADO
   ============================================================ */

function actualizarDatosProductoOrden() {

    const productoId =
        Number(
            document
                .getElementById("orderProduct")
                .value
        );


    const producto =
        ordenProductos.find(
            item =>
                Number(item.id) === productoId
        );


    if (!producto) {
        return;
    }


    document
        .getElementById("orderUnitPrice")
        .value =
            Number(producto.precio || 0);


    if (producto.proveedorPrincipalId) {

        document
            .getElementById("orderSupplier")
            .value =
                producto.proveedorPrincipalId;
    }
}


/* ============================================================
   CARGAR ÓRDENES
   ============================================================ */

async function cargarOrdenes() {

    const filtro =
        document
            .getElementById("orderStatusFilter")
            .value;


    const ruta =
        filtro
            ? API.RUTAS.ORDENES +
              "?estado=" +
              encodeURIComponent(filtro)
            : API.RUTAS.ORDENES;


    try {

        ordenesActuales =
            await peticionApi(ruta);


        if (!esModuloVigente("orden")) {
            return;
        }


        filtrarOrdenes();

    } catch (error) {

        if (!esModuloVigente("orden")) {
            return;
        }


        mostrarToast(
            error.message,
            "error"
        );


        renderizarOrdenes([]);
    }
}


/* ============================================================
   FILTRAR
   ============================================================ */

function filtrarOrdenes() {

    const input =
        document.getElementById("orderSearch");


    const texto =
        input
            ? input.value
                .trim()
                .toLowerCase()
            : "";


    const filtradas =
        ordenesActuales.filter(item => {

            const producto =
                String(
                    item.nombreProducto || ""
                ).toLowerCase();

            const proveedor =
                String(
                    item.nombreProveedor || ""
                ).toLowerCase();

            const bodega =
                String(
                    item.nombreBodegaDestino || ""
                ).toLowerCase();

            const id =
                String(item.id || "");


            return (
                producto.includes(texto) ||
                proveedor.includes(texto) ||
                bodega.includes(texto) ||
                id.includes(texto)
            );
        });


    renderizarOrdenes(filtradas);
}


/* ============================================================
   RENDER TABLA
   ============================================================ */

function renderizarOrdenes(lista) {

    const body =
        document.getElementById(
            "orderTableBody"
        );


    document
        .getElementById("orderCount")
        .textContent =
            lista.length +
            (
                lista.length === 1
                    ? " orden"
                    : " órdenes"
            );


    document
        .getElementById(
            "orderTableFooter"
        )
        .textContent =
            lista.length +
            (
                lista.length === 1
                    ? " orden mostrada"
                    : " órdenes mostradas"
            );


    if (lista.length === 0) {

        body.innerHTML = `
            <tr>
                <td colspan="10">
                    <div class="empty-state">
                        No hay órdenes para mostrar.
                    </div>
                </td>
            </tr>
        `;

        return;
    }


    body.innerHTML =
        lista.map(item => `

            <tr>

                <td>
                    #${item.id}
                </td>

                <td>
                    ${formatoFecha(item.fechaCreacion)}
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
                    <strong>
                        ${formatoMoneda(
                            item.total || 0
                        )}
                    </strong>
                </td>

                <td>
                    ${badgeEstadoOrden(item.estado)}
                </td>

                <td>
                    ${
                        item.pdfDisponible
                            ? `
                                <span class="badge badge-green">
                                    Disponible
                                </span>
                              `
                            : `
                                <span class="badge badge-amber">
                                    Sin generar
                                </span>
                              `
                    }
                </td>

                <td>
                    ${accionesOrden(item)}
                </td>

            </tr>

        `).join("");
}


/* ============================================================
   BADGES
   ============================================================ */

function badgeEstadoOrden(estado) {

    const clase =
        estado === "BORRADOR"
            ? "badge-blue"
            : estado === "APROBADA"
                ? "badge-amber"
                : estado === "RECIBIDA"
                    ? "badge-green"
                    : "badge-red";


    return `
        <span class="badge ${clase}">
            ${escaparHtml(estado || "")}
        </span>
    `;
}


/* ============================================================
   ACCIONES
   ============================================================ */

function accionesOrden(item) {

    let html = `
        <div class="table-actions">

            <button
                class="action-button"
                onclick="verDetalleOrden(${item.id})"
                title="Ver detalle"
                aria-label="Ver detalle">
                ◎
            </button>

            <button
                class="action-button"
                onclick="generarPdfOrden(${item.id})"
                title="Generar PDF"
                aria-label="Generar PDF">
                PDF
            </button>
    `;


    if (item.pdfDisponible) {

        html += `
            <button
                class="action-button"
                onclick="verPdfOrden(${item.id})"
                title="Ver PDF"
                aria-label="Ver PDF">
                ↗
            </button>
        `;
    }


    if (esAdmin()) {

        if (item.estado === "BORRADOR") {

            html += `
                <button
                    class="action-button"
                    onclick="cambiarEstadoOrden(
                        ${item.id},
                        'APROBADA'
                    )"
                    title="Aprobar orden"
                    aria-label="Aprobar orden">
                    ✓
                </button>

                <button
                    class="action-button danger"
                    onclick="cambiarEstadoOrden(
                        ${item.id},
                        'CANCELADA'
                    )"
                    title="Cancelar orden"
                    aria-label="Cancelar orden">
                    ×
                </button>
            `;
        }


        if (item.estado === "APROBADA") {

            html += `
                <button
                    class="action-button"
                    onclick="cambiarEstadoOrden(
                        ${item.id},
                        'RECIBIDA'
                    )"
                    title="Registrar recepción"
                    aria-label="Registrar recepción">
                    ↓
                </button>

                <button
                    class="action-button danger"
                    onclick="cambiarEstadoOrden(
                        ${item.id},
                        'CANCELADA'
                    )"
                    title="Cancelar orden"
                    aria-label="Cancelar orden">
                    ×
                </button>
            `;
        }
    }


    html += "</div>";

    return html;
}


/* ============================================================
   CREAR ORDEN
   ============================================================ */

function abrirOrdenNueva() {

    document
        .getElementById("orderForm")
        .reset();


    document
        .getElementById("orderQuantity")
        .value = 1;


    abrirModal("orderModal");
}


async function guardarOrden(evento) {

    evento.preventDefault();


    if (!puedeCrearOrden()) {

        mostrarToast(
            "No tienes permisos para crear órdenes.",
            "error"
        );

        return;
    }


    const orden = {

        productoId:
            Number(
                document
                    .getElementById("orderProduct")
                    .value
            ),

        proveedorId:
            Number(
                document
                    .getElementById("orderSupplier")
                    .value
            ),

        bodegaDestinoId:
            Number(
                document
                    .getElementById("orderWarehouse")
                    .value
            ),

        cantidad:
            Number(
                document
                    .getElementById("orderQuantity")
                    .value
            ),

        precioUnitario:
            Number(
                document
                    .getElementById("orderUnitPrice")
                    .value
            )
    };


    if (
        !orden.productoId ||
        !orden.proveedorId ||
        !orden.bodegaDestinoId
    ) {

        mostrarToast(
            "Selecciona producto, proveedor y bodega.",
            "error"
        );

        return;
    }


    if (orden.cantidad <= 0) {

        mostrarToast(
            "La cantidad debe ser mayor a cero.",
            "error"
        );

        return;
    }


    if (orden.precioUnitario <= 0) {

        mostrarToast(
            "El precio unitario debe ser mayor a cero.",
            "error"
        );

        return;
    }


    try {

        const creada =
            await peticionApi(
                API.RUTAS.ORDENES,
                {
                    method: "POST",
                    body: JSON.stringify(orden)
                }
            );


        cerrarModal("orderModal");


        mostrarToast(
            "Orden #" +
            creada.id +
            " creada en estado BORRADOR.",
            "success"
        );


        await cargarOrdenes();

    } catch (error) {

        mostrarToast(
            error.message,
            "error"
        );
    }
}


/* ============================================================
   DETALLE
   ============================================================ */

async function verDetalleOrden(id) {

    try {

        const item =
            await peticionApi(
                API.RUTAS.ORDENES + "/" + id
            );


        document
            .getElementById("orderDetailTitle")
            .textContent =
                "Orden de compra #" + item.id;


        document
            .getElementById("orderDetailBody")
            .innerHTML = `

                <div class="form-grid">

                    ${detalleOrdenCampo(
                        "Estado",
                        item.estado
                    )}

                    ${detalleOrdenCampo(
                        "Fecha creación",
                        formatoFecha(item.fechaCreacion)
                    )}

                    ${detalleOrdenCampo(
                        "Producto",
                        item.nombreProducto
                    )}

                    ${detalleOrdenCampo(
                        "Proveedor",
                        item.nombreProveedor
                    )}

                    ${detalleOrdenCampo(
                        "Bodega destino",
                        item.nombreBodegaDestino
                    )}

                    ${detalleOrdenCampo(
                        "Cantidad",
                        formatoNumero(item.cantidad)
                    )}

                    ${detalleOrdenCampo(
                        "Precio unitario",
                        formatoMoneda(item.precioUnitario)
                    )}

                    ${detalleOrdenCampo(
                        "Total",
                        formatoMoneda(item.total)
                    )}

                    ${detalleOrdenCampo(
                        "Creado por",
                        item.creadoPor
                    )}

                    ${detalleOrdenCampo(
                        "PDF",
                        item.pdfDisponible
                            ? "Generado"
                            : "No generado"
                    )}

                </div>
            `;


        abrirModal("orderDetailModal");

    } catch (error) {

        mostrarToast(
            error.message,
            "error"
        );
    }
}


function detalleOrdenCampo(etiqueta, valor) {

    return `
        <div class="form-group">
            <label>
                ${escaparHtml(etiqueta)}
            </label>

            <div style="
                padding:.65rem 0;
                font-weight:600
            ">
                ${escaparHtml(
                    String(valor ?? "—")
                )}
            </div>
        </div>
    `;
}


/* ============================================================
   CAMBIAR ESTADO
   ============================================================ */

async function cambiarEstadoOrden(
    id,
    nuevoEstado
) {

    if (!esAdmin()) {

        mostrarToast(
            "Solo ADMIN puede cambiar el estado de una orden.",
            "error"
        );

        return;
    }


    const acciones = {

        APROBADA:
            "¿Deseas aprobar esta orden?",

        RECIBIDA:
            "¿Confirmas la recepción? " +
            "Se generará automáticamente una ENTRADA de inventario.",

        CANCELADA:
            "¿Deseas cancelar esta orden?"
    };


    if (
        !window.confirm(
            acciones[nuevoEstado] ||
            "¿Confirmas el cambio de estado?"
        )
    ) {
        return;
    }


    try {

        const actualizada =
            await peticionApi(
                rutaEstadoOrden(id),
                {
                    method: "PATCH",

                    /*
                     * El backend exige EXACTAMENTE
                     * {"estado":"..."}
                     */
                    body: JSON.stringify({
                        estado: nuevoEstado
                    })
                }
            );


        if (nuevoEstado === "RECIBIDA") {

            mostrarToast(
                "Orden recibida. La ENTRADA de inventario fue registrada.",
                "success"
            );

        } else {

            mostrarToast(
                "Orden #" +
                actualizada.id +
                " actualizada a " +
                actualizada.estado +
                ".",
                "success"
            );
        }


        await cargarOrdenes();

    } catch (error) {

        mostrarToast(
            error.message,
            "error"
        );
    }
}


/* ============================================================
   PDF
   ============================================================ */

async function generarPdfOrden(id) {

    const ventana =
        window.open("", "_blank");


    try {

        const blob =
            await peticionPdf(
                rutaOrdenPdf(id),
                {
                    method: "POST"
                }
            );


        abrirBlobPdf(
            blob,
            ventana
        );


        mostrarToast(
            "PDF de la orden #" +
            id +
            " generado correctamente.",
            "success"
        );


        await cargarOrdenes();

    } catch (error) {

        if (ventana) {
            ventana.close();
        }


        mostrarToast(
            error.message,
            "error"
        );
    }
}


async function verPdfOrden(id) {

    const ventana =
        window.open("", "_blank");


    try {

        const blob =
            await peticionPdf(
                rutaOrdenPdf(id),
                {
                    method: "GET"
                }
            );


        abrirBlobPdf(
            blob,
            ventana
        );

    } catch (error) {

        if (ventana) {
            ventana.close();
        }


        mostrarToast(
            error.message,
            "error"
        );
    }
}


function abrirBlobPdf(blob, ventana) {

    const url =
        URL.createObjectURL(blob);


    if (ventana) {

        ventana.location.href = url;

    } else {

        const enlace =
            document.createElement("a");

        enlace.href = url;
        enlace.target = "_blank";

        document.body.appendChild(enlace);

        enlace.click();
        enlace.remove();
    }


    /*
     * Se libera después de un tiempo suficiente
     * para que el visor del navegador lo cargue.
     */
    setTimeout(
        () => URL.revokeObjectURL(url),
        60000
    );
}
