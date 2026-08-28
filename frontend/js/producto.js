let productosActuales = [];
let productoProveedores = [];


/* ============================================================
   INICIAR
   ============================================================ */

async function iniciarProductos() {

    configurarCierreModal("productModal");
    configurarCierreModal("productStockModal");


    const botonNuevo =
        document.getElementById("newProductButton");


    botonNuevo.classList.toggle(
        "hidden",
        !esAdmin()
    );


    if (esAdmin()) {
        botonNuevo.addEventListener(
            "click",
            abrirProductoNuevo
        );
    }


    document
        .getElementById("productForm")
        .addEventListener(
            "submit",
            guardarProducto
        );


    document
        .getElementById("productSearch")
        .addEventListener(
            "input",
            filtrarProductos
        );


    try {

        const resultados =
            await Promise.all([
                peticionApi(API.RUTAS.PRODUCTOS),
                peticionApi(API.RUTAS.PROVEEDORES)
            ]);


        productosActuales = resultados[0];
        productoProveedores = resultados[1];


        if (!esModuloVigente("producto")) {
            return;
        }


        llenarProveedoresProducto();

        renderizarProductos(
            productosActuales
        );

    } catch (error) {

        if (!esModuloVigente("producto")) {
            return;
        }


        mostrarToast(
            error.message,
            "error"
        );


        renderizarProductos([]);
    }
}


/* ============================================================
   PROVEEDORES
   ============================================================ */

function llenarProveedoresProducto() {

    const select =
        document.getElementById(
            "productSupplier"
        );


    select.innerHTML = `
        <option value="">
            Sin proveedor principal
        </option>
        ${
            productoProveedores.map(item => `
                <option value="${item.id}">
                    ${escaparHtml(item.nombre)}
                    · ${item.diasEntrega} días
                </option>
            `).join("")
        }
    `;
}


/* ============================================================
   CARGAR
   ============================================================ */

async function cargarProductos() {

    try {

        productosActuales =
            await peticionApi(
                API.RUTAS.PRODUCTOS
            );


        if (!esModuloVigente("producto")) {
            return;
        }


        filtrarProductos();

    } catch (error) {

        mostrarToast(
            error.message,
            "error"
        );
    }
}


/* ============================================================
   RENDER
   ============================================================ */

function renderizarProductos(lista) {

    const body =
        document.getElementById(
            "productTableBody"
        );


    const bajos =
        lista.filter(
            item =>
                Number(item.stockTotal) < 10
        ).length;


    document
        .getElementById("lowStockCount")
        .textContent =
            bajos + " con stock bajo";


    document
        .getElementById("productCount")
        .textContent =
            lista.length +
            (
                lista.length === 1
                    ? " producto"
                    : " productos"
            );


    document
        .getElementById(
            "productTableFooter"
        )
        .textContent =
            lista.length +
            (
                lista.length === 1
                    ? " producto mostrado"
                    : " productos mostrados"
            );


    if (lista.length === 0) {

        body.innerHTML = `
            <tr>
                <td colspan="7">
                    <div class="empty-state">
                        No hay productos para mostrar.
                    </div>
                </td>
            </tr>
        `;

        return;
    }


    body.innerHTML =
        lista.map(item => {

            const estado =
                estadoStock(item.stockTotal);


            const proveedor =
                item.nombreProveedorPrincipal
                    ? `
                        <span class="badge badge-blue">
                            ${escaparHtml(
                                item.nombreProveedorPrincipal
                            )}
                        </span>
                      `
                    : `
                        <span class="badge badge-amber">
                            Sin proveedor
                        </span>
                      `;


            return `
                <tr>

                    <td>
                        #${item.id}
                    </td>

                    <td class="cell-title">
                        ${escaparHtml(item.nombre)}
                    </td>

                    <td>
                        <span class="badge badge-blue">
                            ${escaparHtml(item.categoria)}
                        </span>
                    </td>

                    <td>
                        ${proveedor}
                    </td>

                    <td>
                        <span class="badge ${estado.clase}">
                            ${formatoNumero(item.stockTotal)}
                            ·
                            ${estado.texto}
                        </span>
                    </td>

                    <td>
                        ${formatoMoneda(item.precio)}
                    </td>

                    <td>

                        <div class="table-actions">

                            <button
                                class="action-button"
                                onclick="verStockProducto(${item.id})"
                                title="Ver stock por bodega"
                                aria-label="Ver stock por bodega">
                                ◎
                            </button>

                            ${
                                esAdmin()
                                    ? `
                                        <button
                                            class="action-button"
                                            onclick="editarProducto(${item.id})"
                                            title="Editar producto"
                                            aria-label="Editar producto">
                                            ✎
                                        </button>

                                        <button
                                            class="action-button danger"
                                            onclick="eliminarProducto(${item.id})"
                                            title="Eliminar producto"
                                            aria-label="Eliminar producto">
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
   BUSCADOR
   ============================================================ */

function normalizarBusquedaProducto(valor) {

    return String(valor ?? "")
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .trim();
}


function filtrarProductos() {

    const texto =
        normalizarBusquedaProducto(
            document
                .getElementById("productSearch")
                .value
        );


    const filtrados =
        productosActuales.filter(item => {

            const contenido =
                normalizarBusquedaProducto([
                    item.id,
                    item.nombre,
                    item.categoria,
                    item.nombreProveedorPrincipal,
                    item.stockTotal,
                    item.precio
                ].join(" "));


            return (
                !texto ||
                contenido.includes(texto)
            );
        });


    renderizarProductos(filtrados);
}


/* ============================================================
   NUEVO / EDITAR
   ============================================================ */

function abrirProductoNuevo() {

    document
        .getElementById("productForm")
        .reset();


    document
        .getElementById("productId")
        .value = "";


    document
        .getElementById(
            "productModalTitle"
        )
        .textContent =
            "Nuevo producto";


    abrirModal("productModal");
}


function editarProducto(id) {

    const producto =
        productosActuales.find(
            item =>
                Number(item.id) === Number(id)
        );


    if (!producto) {
        return;
    }


    document
        .getElementById("productId")
        .value =
            producto.id;


    document
        .getElementById("productName")
        .value =
            producto.nombre;


    document
        .getElementById("productCategory")
        .value =
            producto.categoria;


    document
        .getElementById("productPrice")
        .value =
            producto.precio;


    document
        .getElementById("productSupplier")
        .value =
            producto.proveedorPrincipalId || "";


    document
        .getElementById(
            "productModalTitle"
        )
        .textContent =
            "Editar producto";


    abrirModal("productModal");
}


/* ============================================================
   GUARDAR
   ============================================================ */

async function guardarProducto(evento) {

    evento.preventDefault();


    if (!esAdmin()) {

        mostrarToast(
            "No tienes permisos para modificar productos.",
            "error"
        );

        return;
    }


    const id =
        document
            .getElementById("productId")
            .value;


    const proveedorValor =
        document
            .getElementById("productSupplier")
            .value;


    const producto = {

        nombre:
            document
                .getElementById("productName")
                .value
                .trim(),

        categoria:
            document
                .getElementById("productCategory")
                .value
                .trim(),

        precio:
            Number(
                document
                    .getElementById("productPrice")
                    .value
            ),

        proveedorPrincipalId:
            proveedorValor
                ? Number(proveedorValor)
                : null
    };


    try {

        await peticionApi(
            id
                ? API.RUTAS.PRODUCTOS + "/" + id
                : API.RUTAS.PRODUCTOS,
            {
                method: id ? "PUT" : "POST",
                body: JSON.stringify(producto)
            }
        );


        cerrarModal("productModal");


        mostrarToast(
            id
                ? "Producto actualizado correctamente."
                : "Producto creado correctamente.",
            "success"
        );


        await cargarProductos();

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

async function eliminarProducto(id) {

    if (!esAdmin()) {
        return;
    }


    const producto =
        productosActuales.find(
            item =>
                Number(item.id) === Number(id)
        );


    if (!producto) {
        return;
    }


    if (
        !window.confirm(
            "¿Deseas eliminar \"" +
            producto.nombre +
            "\"?\n\n" +
            "Si posee movimientos, inventario histórico " +
            "u órdenes de compra, el backend impedirá eliminarlo."
        )
    ) {
        return;
    }


    try {

        await peticionApi(
            API.RUTAS.PRODUCTOS + "/" + id,
            {
                method: "DELETE"
            }
        );


        mostrarToast(
            "Producto eliminado.",
            "success"
        );


        await cargarProductos();

    } catch (error) {

        if (error.status === 409) {

            mostrarToast(
                "El producto posee información histórica y no puede eliminarse.",
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
   STOCK REAL POR BODEGA
   ============================================================ */

async function verStockProducto(id) {

    try {

        const stock =
            await peticionApi(
                rutaStockProducto(id)
            );


        document
            .getElementById("productStockTitle")
            .textContent =
                stock.nombreProducto +
                " · Stock por bodega";


        const bodegas =
            Array.isArray(stock.bodegas)
                ? stock.bodegas
                : [];


        document
            .getElementById("productStockBody")
            .innerHTML = `

                <div
                    style="
                        display:flex;
                        justify-content:space-between;
                        align-items:center;
                        gap:1rem;
                        margin-bottom:1rem
                    ">

                    <div>
                        <strong>
                            Stock total
                        </strong>

                        <div style="
                            font-size:1.8rem;
                            font-weight:800;
                            margin-top:.25rem
                        ">
                            ${formatoNumero(stock.stockTotal)}
                        </div>
                    </div>

                    <div style="text-align:right">
                        <small>
                            Precio
                        </small>

                        <div style="
                            font-weight:700;
                            margin-top:.25rem
                        ">
                            ${formatoMoneda(stock.precio)}
                        </div>
                    </div>

                </div>


                <div class="table-card">

                    <div class="table-scroll">

                        <table>

                            <thead>
                                <tr>
                                    <th>Bodega</th>
                                    <th>Existencias</th>
                                    <th>Estado</th>
                                </tr>
                            </thead>

                            <tbody>

                                ${
                                    bodegas.map(item => {

                                        const estado =
                                            estadoStock(
                                                item.stock
                                            );

                                        return `
                                            <tr>

                                                <td class="cell-title">
                                                    ${escaparHtml(
                                                        item.nombreBodega
                                                    )}
                                                </td>

                                                <td>
                                                    ${formatoNumero(
                                                        item.stock
                                                    )}
                                                </td>

                                                <td>
                                                    <span
                                                        class="badge ${estado.clase}">
                                                        ${estado.texto}
                                                    </span>
                                                </td>

                                            </tr>
                                        `;

                                    }).join("")
                                }

                            </tbody>

                        </table>

                    </div>

                </div>
            `;


        abrirModal(
            "productStockModal"
        );

    } catch (error) {

        mostrarToast(
            error.message,
            "error"
        );
    }
}
