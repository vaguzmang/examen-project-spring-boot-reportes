let bodegasActuales = [];
let ocupacionBodegasActual = [];


/* ============================================================
   INICIAR
   ============================================================ */

async function iniciarBodegas() {

    configurarCierreModal("warehouseModal");


    const botonNuevo =
        document.getElementById(
            "newWarehouseButton"
        );


    botonNuevo.classList.toggle(
        "hidden",
        !esAdmin()
    );


    if (esAdmin()) {

        botonNuevo.addEventListener(
            "click",
            abrirBodegaNueva
        );
    }


    document
        .getElementById("warehouseForm")
        .addEventListener(
            "submit",
            guardarBodega
        );


    document
        .getElementById("warehouseSearch")
        .addEventListener(
            "input",
            filtrarBodegas
        );


    await cargarBodegas();
}


/* ============================================================
   CARGAR
   ============================================================ */

async function cargarBodegas() {

    try {

        const resultados =
            await Promise.all([
                peticionApi(API.RUTAS.BODEGAS),
                peticionApi(API.RUTAS.KPIS)
            ]);


        bodegasActuales =
            resultados[0];


        ocupacionBodegasActual =
            resultados[1].ocupacionBodegas || [];


        if (!esModuloVigente("bodega")) {
            return;
        }


        filtrarBodegas();

    } catch (error) {

        if (!esModuloVigente("bodega")) {
            return;
        }


        mostrarToast(
            error.message,
            "error"
        );


        renderizarBodegas([]);
    }
}


/* ============================================================
   OCUPACIÓN
   ============================================================ */

function obtenerOcupacionBodega(id) {

    return ocupacionBodegasActual.find(
        item =>
            Number(item.bodegaId) ===
            Number(id)
    ) || {
        unidades: 0,
        capacidad: 0,
        ocupacion: 0
    };
}


function estadoOcupacionBodega(porcentaje) {

    const valor =
        Number(porcentaje || 0);


    if (valor >= 90) {

        return {
            texto: "Crítica",
            clase: "badge-red"
        };
    }


    if (valor >= 75) {

        return {
            texto: "Alta",
            clase: "badge-amber"
        };
    }


    return {
        texto: "Normal",
        clase: "badge-green"
    };
}


/* ============================================================
   RENDER
   ============================================================ */

function renderizarBodegas(lista) {

    const body =
        document.getElementById(
            "warehouseTableBody"
        );


    const criticas =
        lista.filter(item => {

            const ocupacion =
                obtenerOcupacionBodega(
                    item.id
                );

            return Number(
                ocupacion.ocupacion || 0
            ) >= 90;

        }).length;


    document
        .getElementById(
            "criticalWarehouseCount"
        )
        .textContent =
            criticas +
            (
                criticas === 1
                    ? " crítica"
                    : " críticas"
            );


    document
        .getElementById("warehouseCount")
        .textContent =
            lista.length +
            (
                lista.length === 1
                    ? " bodega"
                    : " bodegas"
            );


    document
        .getElementById(
            "warehouseTableFooter"
        )
        .textContent =
            lista.length +
            (
                lista.length === 1
                    ? " bodega mostrada"
                    : " bodegas mostradas"
            );


    if (lista.length === 0) {

        body.innerHTML = `
            <tr>
                <td colspan="9">
                    <div class="empty-state">
                        No hay bodegas para mostrar.
                    </div>
                </td>
            </tr>
        `;

        return;
    }


    body.innerHTML =
        lista.map(item => {

            const ocupacion =
                obtenerOcupacionBodega(
                    item.id
                );


            const porcentaje =
                Number(
                    ocupacion.ocupacion || 0
                );


            const estado =
                estadoOcupacionBodega(
                    porcentaje
                );


            const capacidad =
                Number(item.capacidad || 0);


            const unidades =
                Number(
                    ocupacion.unidades || 0
                );


            return `
                <tr>

                    <td>
                        #${item.id}
                    </td>

                    <td class="cell-title">
                        ${escaparHtml(item.nombre)}
                    </td>

                    <td>
                        ${escaparHtml(item.ubicacion)}
                    </td>

                    <td>
                        ${escaparHtml(item.encargado)}
                    </td>

                    <td>
                        ${formatoNumero(unidades)}
                    </td>

                    <td>
                        ${formatoNumero(capacidad)}
                    </td>

                    <td>

                        <div style="
                            display:grid;
                            gap:.35rem;
                            min-width:105px
                        ">

                            <strong>
                                ${porcentaje.toFixed(2)}%
                            </strong>

                            <div class="bar-track">
                                <span
                                    class="bar-fill"
                                    style="
                                        width:${Math.min(
                                            Math.max(
                                                porcentaje,
                                                0
                                            ),
                                            100
                                        )}%
                                    ">
                                </span>
                            </div>

                        </div>

                    </td>

                    <td>
                        <span
                            class="badge ${estado.clase}">
                            ${estado.texto}
                        </span>
                    </td>

                    <td>

                        <div class="table-actions">

                            <button
                                class="action-button"
                                onclick="verInventarioBodega(${item.id})"
                                title="Ver inventario de esta bodega"
                                aria-label="Ver inventario">
                                ◎
                            </button>

                            ${
                                esAdmin()
                                    ? `
                                        <button
                                            class="action-button"
                                            onclick="editarBodega(${item.id})"
                                            title="Editar bodega"
                                            aria-label="Editar bodega">
                                            ✎
                                        </button>

                                        <button
                                            class="action-button danger"
                                            onclick="eliminarBodega(${item.id})"
                                            title="Eliminar bodega"
                                            aria-label="Eliminar bodega">
                                            ×
                                        </button>
                                      `
                                    : ""
                            }

                        </div>

                    </td>

                </tr>
            `;

        }).join("");
}


/* ============================================================
   BUSCAR
   ============================================================ */

function normalizarBusquedaBodega(valor) {

    return String(valor ?? "")
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .trim();
}


function filtrarBodegas() {

    const texto =
        normalizarBusquedaBodega(
            document
                .getElementById(
                    "warehouseSearch"
                )
                .value
        );


    const filtradas =
        bodegasActuales.filter(item => {

            const ocupacion =
                obtenerOcupacionBodega(
                    item.id
                );


            const contenido =
                normalizarBusquedaBodega([
                    item.id,
                    item.nombre,
                    item.ubicacion,
                    item.encargado,
                    item.capacidad,
                    ocupacion.unidades,
                    ocupacion.ocupacion
                ].join(" "));


            return (
                !texto ||
                contenido.includes(texto)
            );
        });


    renderizarBodegas(
        filtradas
    );
}


/* ============================================================
   CREAR / EDITAR
   ============================================================ */

function abrirBodegaNueva() {

    document
        .getElementById("warehouseForm")
        .reset();


    document
        .getElementById("warehouseId")
        .value = "";


    document
        .getElementById(
            "warehouseModalTitle"
        )
        .textContent =
            "Nueva bodega";


    abrirModal("warehouseModal");
}


function editarBodega(id) {

    const bodega =
        bodegasActuales.find(
            item =>
                Number(item.id) === Number(id)
        );


    if (!bodega) {
        return;
    }


    document
        .getElementById("warehouseId")
        .value =
            bodega.id;


    document
        .getElementById("warehouseName")
        .value =
            bodega.nombre;


    document
        .getElementById("warehouseLocation")
        .value =
            bodega.ubicacion;


    document
        .getElementById("warehouseCapacity")
        .value =
            bodega.capacidad;


    document
        .getElementById("warehouseManager")
        .value =
            bodega.encargado;


    document
        .getElementById(
            "warehouseModalTitle"
        )
        .textContent =
            "Editar bodega";


    abrirModal("warehouseModal");
}


/* ============================================================
   GUARDAR
   ============================================================ */

async function guardarBodega(evento) {

    evento.preventDefault();


    if (!esAdmin()) {
        return;
    }


    const id =
        document
            .getElementById("warehouseId")
            .value;


    const bodega = {

        nombre:
            document
                .getElementById("warehouseName")
                .value
                .trim(),

        ubicacion:
            document
                .getElementById("warehouseLocation")
                .value
                .trim(),

        capacidad:
            Number(
                document
                    .getElementById(
                        "warehouseCapacity"
                    )
                    .value
            ),

        encargado:
            document
                .getElementById("warehouseManager")
                .value
                .trim()
    };


    if (
        !Number.isInteger(bodega.capacidad) ||
        bodega.capacidad < 0
    ) {

        mostrarToast(
            "La capacidad debe ser un número entero no negativo.",
            "error"
        );

        return;
    }


    try {

        await peticionApi(
            id
                ? API.RUTAS.BODEGAS + "/" + id
                : API.RUTAS.BODEGAS,
            {
                method:
                    id
                        ? "PUT"
                        : "POST",

                body:
                    JSON.stringify(bodega)
            }
        );


        cerrarModal("warehouseModal");


        mostrarToast(
            id
                ? "Bodega actualizada correctamente."
                : "Bodega creada correctamente.",
            "success"
        );


        await cargarBodegas();

    } catch (error) {

        mostrarToast(
            error.message,
            "error"
        );
    }
}


/* ============================================================
   ELIMINAR
   ============================================================ */

async function eliminarBodega(id) {

    if (!esAdmin()) {
        return;
    }


    const bodega =
        bodegasActuales.find(
            item =>
                Number(item.id) === Number(id)
        );


    if (!bodega) {
        return;
    }


    if (
        !window.confirm(
            "¿Deseas eliminar \"" +
            bodega.nombre +
            "\"?\n\n" +
            "Si posee movimientos, inventario u órdenes, " +
            "el backend impedirá eliminarla."
        )
    ) {
        return;
    }


    try {

        await peticionApi(
            API.RUTAS.BODEGAS + "/" + id,
            {
                method: "DELETE"
            }
        );


        mostrarToast(
            "Bodega eliminada.",
            "success"
        );


        await cargarBodegas();

    } catch (error) {

        if (error.status === 409) {

            mostrarToast(
                "La bodega posee información histórica y no puede eliminarse.",
                "error"
            );

            return;
        }


        mostrarToast(
            error.message,
            "error"
        );
    }
}


/* ============================================================
   IR AL INVENTARIO DE LA BODEGA
   ============================================================ */

function verInventarioBodega(id) {

    sessionStorage.setItem(
        "logitrack_inventory_warehouse_filter",
        String(id)
    );


    abrirModulo(
        "inventario"
    );
}
