let inventarioActual = [];
let inventarioBodegas = [];
let inventarioProductos = [];
let inventarioStocksProducto = new Map();


/* ============================================================
   INICIAR
   ============================================================ */

async function iniciarInventario() {

    configurarCierreModal(
        "inventoryOperationModal"
    );


    document
        .getElementById("inventoryMainActions")
        .classList.toggle(
            "hidden",
            !esAdmin()
        );


    if (esAdmin()) {

        document
            .getElementById("inventoryEntryButton")
            .addEventListener(
                "click",
                () =>
                    abrirOperacionInventario(
                        "ENTRADA"
                    )
            );


        document
            .getElementById("inventoryExitButton")
            .addEventListener(
                "click",
                () =>
                    abrirOperacionInventario(
                        "SALIDA"
                    )
            );


        document
            .getElementById("inventoryTransferButton")
            .addEventListener(
                "click",
                () =>
                    abrirOperacionInventario(
                        "TRANSFERENCIA"
                    )
            );
    }


    document
        .getElementById("inventorySearch")
        .addEventListener(
            "input",
            filtrarInventario
        );


    document
        .getElementById("warehouseFilter")
        .addEventListener(
            "change",
            filtrarInventario
        );


    document
        .getElementById("lowStockToggle")
        .addEventListener(
            "change",
            filtrarInventario
        );


    document
        .getElementById("inventoryOperationForm")
        .addEventListener(
            "submit",
            guardarOperacionInventario
        );


    document
        .getElementById("inventoryOperationProduct")
        .addEventListener(
            "change",
            actualizarDisponibilidadInventario
        );


    document
        .getElementById("inventoryOrigin")
        .addEventListener(
            "change",
            actualizarDisponibilidadInventario
        );


    try {

        const resultados =
            await Promise.all([
                peticionApi(API.RUTAS.BODEGAS),
                peticionApi(API.RUTAS.PRODUCTOS)
            ]);


        inventarioBodegas = resultados[0];
        inventarioProductos = resultados[1];


        if (!esModuloVigente("inventario")) {
            return;
        }


        llenarSelectsInventario();

        await cargarInventario();

    } catch (error) {

        mostrarToast(
            error.message,
            "error"
        );
    }
}


/* ============================================================
   CARGA DEL STOCK OFICIAL
   ============================================================ */

async function cargarInventario() {

    try {

        /*
         * Fuente real de stock:
         * GET /productos/{id}/stock
         *
         * No usamos edición directa de InventarioBodega.
         */
        const stocks =
            await Promise.all(
                inventarioProductos.map(
                    producto =>
                        peticionApi(
                            rutaStockProducto(
                                producto.id
                            )
                        )
                )
            );


        if (!esModuloVigente("inventario")) {
            return;
        }


        inventarioStocksProducto =
            new Map();


        const filas = [];


        stocks.forEach(stockProducto => {

            inventarioStocksProducto.set(
                Number(stockProducto.productoId),
                stockProducto
            );


            (stockProducto.bodegas || [])
                .forEach(bodega => {

                    filas.push({

                        productoId:
                            stockProducto.productoId,

                        productoNombre:
                            stockProducto.nombreProducto,

                        precio:
                            stockProducto.precio,

                        bodegaId:
                            bodega.bodegaId,

                        bodegaNombre:
                            bodega.nombreBodega,

                        stock:
                            Number(
                                bodega.stock || 0
                            )
                    });
                });
        });


        inventarioActual =
            filas.sort((a, b) => {

                const porBodega =
                    a.bodegaNombre.localeCompare(
                        b.bodegaNombre,
                        "es"
                    );

                if (porBodega !== 0) {
                    return porBodega;
                }

                return a.productoNombre.localeCompare(
                    b.productoNombre,
                    "es"
                );
            });


        filtrarInventario();

    } catch (error) {

        if (!esModuloVigente("inventario")) {
            return;
        }


        mostrarToast(
            error.message,
            "error"
        );


        renderizarInventario([]);
    }
}


/* ============================================================
   SELECTS
   ============================================================ */

function llenarSelectsInventario() {

    const opcionesBodega =
        '<option value="">Selecciona una bodega</option>' +
        inventarioBodegas
            .map(item => `
                <option value="${item.id}">
                    ${escaparHtml(item.nombre)}
                </option>
            `)
            .join("");


    document
        .getElementById("warehouseFilter")
        .innerHTML =
            '<option value="">Todas las bodegas</option>' +
            inventarioBodegas
                .map(item => `
                    <option value="${item.id}">
                        ${escaparHtml(item.nombre)}
                    </option>
                `)
                .join("");


    document
        .getElementById("inventoryOrigin")
        .innerHTML =
            opcionesBodega;


    document
        .getElementById("inventoryDestination")
        .innerHTML =
            opcionesBodega;


    document
        .getElementById("inventoryOperationProduct")
        .innerHTML =
            '<option value="">Selecciona un producto</option>' +
            inventarioProductos
                .map(item => `
                    <option value="${item.id}">
                        ${escaparHtml(item.nombre)}
                    </option>
                `)
                .join("");
}


/* ============================================================
   FILTRO
   ============================================================ */

function normalizarBusquedaInventario(valor) {

    return String(valor ?? "")
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .trim();
}


function filtrarInventario() {

    const texto =
        normalizarBusquedaInventario(
            document
                .getElementById("inventorySearch")
                .value
        );


    const bodegaId =
        document
            .getElementById("warehouseFilter")
            .value;


    const soloBajo =
        document
            .getElementById("lowStockToggle")
            .checked;


    const filtrados =
        inventarioActual.filter(item => {

            const contenido =
                normalizarBusquedaInventario([
                    item.productoNombre,
                    item.bodegaNombre,
                    item.stock
                ].join(" "));


            const coincideTexto =
                !texto ||
                contenido.includes(texto);


            const coincideBodega =
                !bodegaId ||
                Number(item.bodegaId) ===
                Number(bodegaId);


            const coincideStock =
                !soloBajo ||
                Number(item.stock) < 10;


            return (
                coincideTexto &&
                coincideBodega &&
                coincideStock
            );
        });


    renderizarInventario(
        filtrados
    );
}


/* ============================================================
   TABLA
   ============================================================ */

function renderizarInventario(lista) {

    const body =
        document.getElementById(
            "inventoryTableBody"
        );


    const total =
        lista.reduce(
            (suma, item) =>
                suma + Number(item.stock),
            0
        );


    document
        .getElementById("inventoryCount")
        .textContent =
            lista.length +
            (
                lista.length === 1
                    ? " registro"
                    : " registros"
            );


    document
        .getElementById(
            "inventoryTableFooter"
        )
        .textContent =
            lista.length +
            " registros · " +
            formatoNumero(total) +
            " unidades";


    if (lista.length === 0) {

        body.innerHTML = `
            <tr>
                <td colspan="5">
                    <div class="empty-state">
                        No hay inventario para mostrar.
                    </div>
                </td>
            </tr>
        `;

        return;
    }


    body.innerHTML =
        lista.map(item => {

            const estado =
                estadoStock(item.stock);


            return `
                <tr>

                    <td class="cell-title">
                        ${escaparHtml(
                            item.bodegaNombre
                        )}
                    </td>

                    <td>
                        ${escaparHtml(
                            item.productoNombre
                        )}
                    </td>

                    <td>
                        <strong>
                            ${formatoNumero(
                                item.stock
                            )}
                        </strong>
                    </td>

                    <td>
                        <span
                            class="badge ${estado.clase}">
                            ${estado.texto}
                        </span>
                    </td>

                    <td>

                        ${
                            esAdmin()
                                ? `
                                    <div class="table-actions">

                                        <button
                                            class="action-button"
                                            onclick="abrirOperacionInventario(
                                                'ENTRADA',
                                                ${item.productoId},
                                                ${item.bodegaId}
                                            )"
                                            title="Ingresar existencias">
                                            +
                                        </button>

                                        <button
                                            class="action-button"
                                            onclick="abrirOperacionInventario(
                                                'SALIDA',
                                                ${item.productoId},
                                                ${item.bodegaId}
                                            )"
                                            title="Retirar existencias">
                                            −
                                        </button>

                                        <button
                                            class="action-button"
                                            onclick="abrirOperacionInventario(
                                                'TRANSFERENCIA',
                                                ${item.productoId},
                                                ${item.bodegaId}
                                            )"
                                            title="Transferir a otra bodega">
                                            ⇄
                                        </button>

                                    </div>
                                  `
                                : `
                                    <span style="
                                        color:var(--text-muted,#6b7280);
                                        font-size:.85rem
                                    ">
                                        Solo lectura
                                    </span>
                                  `
                        }

                    </td>

                </tr>
            `;

        }).join("");
}


/* ============================================================
   ABRIR OPERACIÓN
   ============================================================ */

function abrirOperacionInventario(
    tipo,
    productoId = null,
    bodegaId = null
) {

    if (!esAdmin()) {

        mostrarToast(
            "Solo ADMIN puede registrar movimientos manuales.",
            "error"
        );

        return;
    }


    document
        .getElementById(
            "inventoryOperationForm"
        )
        .reset();


    document
        .getElementById(
            "inventoryOperationType"
        )
        .value =
            tipo;


    document
        .getElementById(
            "inventoryQuantity"
        )
        .value =
            1;


    if (productoId) {

        document
            .getElementById(
                "inventoryOperationProduct"
            )
            .value =
                productoId;
    }


    const origenGrupo =
        document.getElementById(
            "inventoryOriginGroup"
        );


    const destinoGrupo =
        document.getElementById(
            "inventoryDestinationGroup"
        );


    const disponibleGrupo =
        document.getElementById(
            "inventoryAvailableGroup"
        );


    if (tipo === "ENTRADA") {

        origenGrupo.classList.add(
            "hidden"
        );

        destinoGrupo.classList.remove(
            "hidden"
        );

        disponibleGrupo.classList.add(
            "hidden"
        );


        if (bodegaId) {
            document
                .getElementById(
                    "inventoryDestination"
                )
                .value =
                    bodegaId;
        }


        document
            .getElementById(
                "inventoryOperationTitle"
            )
            .textContent =
                "Ingresar existencias";


        document
            .getElementById(
                "inventoryOperationSubmit"
            )
            .textContent =
                "Registrar entrada";


        document
            .getElementById(
                "inventoryOperationHelp"
            )
            .innerHTML =
                "Esta operación genera un movimiento " +
                "<strong>ENTRADA</strong>. " +
                "Las unidades se sumarán a la bodega destino.";
    }


    if (tipo === "SALIDA") {

        origenGrupo.classList.remove(
            "hidden"
        );

        destinoGrupo.classList.add(
            "hidden"
        );

        disponibleGrupo.classList.remove(
            "hidden"
        );


        if (bodegaId) {
            document
                .getElementById(
                    "inventoryOrigin"
                )
                .value =
                    bodegaId;
        }


        document
            .getElementById(
                "inventoryOperationTitle"
            )
            .textContent =
                "Retirar existencias";


        document
            .getElementById(
                "inventoryOperationSubmit"
            )
            .textContent =
                "Registrar salida";


        document
            .getElementById(
                "inventoryOperationHelp"
            )
            .innerHTML =
                "Esta operación genera un movimiento " +
                "<strong>SALIDA</strong>. " +
                "El sistema impedirá que el stock quede negativo.";
    }


    if (tipo === "TRANSFERENCIA") {

        origenGrupo.classList.remove(
            "hidden"
        );

        destinoGrupo.classList.remove(
            "hidden"
        );

        disponibleGrupo.classList.remove(
            "hidden"
        );


        if (bodegaId) {
            document
                .getElementById(
                    "inventoryOrigin"
                )
                .value =
                    bodegaId;
        }


        document
            .getElementById(
                "inventoryOperationTitle"
            )
            .textContent =
                "Transferir existencias";


        document
            .getElementById(
                "inventoryOperationSubmit"
            )
            .textContent =
                "Registrar transferencia";


        document
            .getElementById(
                "inventoryOperationHelp"
            )
            .innerHTML =
                "La transferencia descuenta unidades de " +
                "la bodega origen y las agrega a la bodega destino.";
    }


    actualizarDisponibilidadInventario();


    abrirModal(
        "inventoryOperationModal"
    );
}


/* ============================================================
   STOCK DISPONIBLE
   ============================================================ */

function actualizarDisponibilidadInventario() {

    const tipo =
        document
            .getElementById(
                "inventoryOperationType"
            )
            .value;


    if (tipo === "ENTRADA") {
        return;
    }


    const productoId =
        Number(
            document
                .getElementById(
                    "inventoryOperationProduct"
                )
                .value
        );


    const bodegaId =
        Number(
            document
                .getElementById(
                    "inventoryOrigin"
                )
                .value
        );


    if (!productoId || !bodegaId) {

        document
            .getElementById(
                "inventoryAvailableStock"
            )
            .textContent =
                "—";

        return;
    }


    const fila =
        inventarioActual.find(
            item =>
                Number(item.productoId) === productoId &&
                Number(item.bodegaId) === bodegaId
        );


    const stock =
        fila
            ? Number(fila.stock)
            : 0;


    document
        .getElementById(
            "inventoryAvailableStock"
        )
        .innerHTML = `
            <span
                class="badge ${
                    stock <= 0
                        ? "badge-red"
                        : stock < 10
                            ? "badge-amber"
                            : "badge-green"
                }">
                ${formatoNumero(stock)} unidades
            </span>
        `;
}


/* ============================================================
   GUARDAR MOVIMIENTO
   ============================================================ */

async function guardarOperacionInventario(
    evento
) {

    evento.preventDefault();


    if (!esAdmin()) {
        return;
    }


    const tipo =
        document
            .getElementById(
                "inventoryOperationType"
            )
            .value;


    const productoId =
        Number(
            document
                .getElementById(
                    "inventoryOperationProduct"
                )
                .value
        );


    const origen =
        document
            .getElementById(
                "inventoryOrigin"
            )
            .value;


    const destino =
        document
            .getElementById(
                "inventoryDestination"
            )
            .value;


    const cantidad =
        Number(
            document
                .getElementById(
                    "inventoryQuantity"
                )
                .value
        );


    if (!productoId) {

        mostrarToast(
            "Selecciona un producto.",
            "error"
        );

        return;
    }


    if (
        !Number.isInteger(cantidad) ||
        cantidad <= 0
    ) {

        mostrarToast(
            "La cantidad debe ser un número entero mayor a cero.",
            "error"
        );

        return;
    }


    if (
        tipo === "TRANSFERENCIA" &&
        Number(origen) === Number(destino)
    ) {

        mostrarToast(
            "La bodega de origen y destino deben ser diferentes.",
            "error"
        );

        return;
    }


    if (
        tipo !== "ENTRADA" &&
        !origen
    ) {

        mostrarToast(
            "Selecciona la bodega de origen.",
            "error"
        );

        return;
    }


    if (
        tipo !== "SALIDA" &&
        !destino
    ) {

        mostrarToast(
            "Selecciona la bodega de destino.",
            "error"
        );

        return;
    }


    /*
     * Validación amigable en frontend.
     * El backend vuelve a validar para garantizar
     * que nunca exista stock negativo.
     */
    if (tipo !== "ENTRADA") {

        const fila =
            inventarioActual.find(
                item =>
                    Number(item.productoId) === productoId &&
                    Number(item.bodegaId) === Number(origen)
            );


        const disponible =
            fila
                ? Number(fila.stock)
                : 0;


        if (cantidad > disponible) {

            mostrarToast(
                "No hay existencias suficientes. Disponible: " +
                formatoNumero(disponible) +
                ".",
                "error"
            );

            return;
        }
    }


    const movimiento = {

        tipo,

        bodegaOrigenId:
            tipo === "ENTRADA"
                ? null
                : Number(origen),

        bodegaDestinoId:
            tipo === "SALIDA"
                ? null
                : Number(destino),

        detalles: [
            {
                productoId,
                cantidad
            }
        ]
    };


    try {

        await peticionApi(
            API.RUTAS.MOVIMIENTOS,
            {
                method: "POST",
                body: JSON.stringify(
                    movimiento
                )
            }
        );


        cerrarModal(
            "inventoryOperationModal"
        );


        const mensajes = {

            ENTRADA:
                "Existencias ingresadas correctamente.",

            SALIDA:
                "Salida registrada correctamente.",

            TRANSFERENCIA:
                "Transferencia registrada correctamente."
        };


        mostrarToast(
            mensajes[tipo],
            "success"
        );


        /*
         * Volvemos a consultar el stock calculado,
         * no incrementamos/decrementamos números
         * manualmente en la interfaz.
         */
        await cargarInventario();

    } catch (error) {

        mostrarToast(
            error.message,
            "error"
        );
    }
}
