let proveedoresActuales = [];


/* ============================================================
   INICIO DEL MÓDULO
   ============================================================ */

async function iniciarProveedores() {

    configurarCierreModal("supplierModal");

    const botonNuevo =
        document.getElementById("newSupplierButton");

    botonNuevo.classList.toggle(
        "hidden",
        !esAdmin()
    );

    if (esAdmin()) {
        botonNuevo.addEventListener(
            "click",
            abrirProveedorNuevo
        );
    }


    document
        .getElementById("supplierForm")
        .addEventListener(
            "submit",
            guardarProveedor
        );


    document
        .getElementById("supplierSearch")
        .addEventListener(
            "input",
            filtrarProveedores
        );


    await cargarProveedores();
}


/* ============================================================
   CARGAR
   ============================================================ */

async function cargarProveedores() {

    try {

        proveedoresActuales =
            await peticionApi(
                API.RUTAS.PROVEEDORES
            );


        if (!esModuloVigente("proveedor")) {
            return;
        }


        renderizarProveedores(
            proveedoresActuales
        );

    } catch (error) {

        if (!esModuloVigente("proveedor")) {
            return;
        }


        mostrarToast(
            error.message,
            "error"
        );


        renderizarProveedores([]);
    }
}


/* ============================================================
   RENDER
   ============================================================ */

function renderizarProveedores(lista) {

    const body =
        document.getElementById(
            "supplierTableBody"
        );


    document
        .getElementById("supplierCount")
        .textContent =
            lista.length +
            (
                lista.length === 1
                    ? " proveedor"
                    : " proveedores"
            );


    document
        .getElementById(
            "supplierTableFooter"
        )
        .textContent =
            lista.length +
            (
                lista.length === 1
                    ? " proveedor registrado"
                    : " proveedores registrados"
            );


    if (lista.length === 0) {

        body.innerHTML = `
            <tr>
                <td colspan="5">
                    <div class="empty-state">
                        No hay proveedores para mostrar.
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

                <td class="cell-title">
                    ${escaparHtml(item.nombre || "")}
                </td>

                <td>
                    ${
                        item.contacto
                            ? escaparHtml(item.contacto)
                            : "—"
                    }
                </td>

                <td>
                    <span class="badge badge-blue">
                        ${formatoNumero(item.diasEntrega)}
                        ${
                            Number(item.diasEntrega) === 1
                                ? "día"
                                : "días"
                        }
                    </span>
                </td>

                <td>

                    ${
                        esAdmin()
                            ? `
                                <div class="table-actions">

                                    <button
                                        class="action-button"
                                        onclick="editarProveedor(${item.id})"
                                        aria-label="Editar proveedor">
                                        ✎
                                    </button>

                                    <button
                                        class="action-button danger"
                                        onclick="eliminarProveedor(${item.id})"
                                        aria-label="Eliminar proveedor">
                                        ×
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

        `).join("");
}


/* ============================================================
   BUSCAR
   ============================================================ */

function filtrarProveedores(evento) {

    const texto =
        evento.target.value
            .trim()
            .toLowerCase();


    const filtrados =
        proveedoresActuales.filter(item => {

            const nombre =
                String(item.nombre || "")
                    .toLowerCase();

            const contacto =
                String(item.contacto || "")
                    .toLowerCase();


            return (
                nombre.includes(texto) ||
                contacto.includes(texto)
            );
        });


    renderizarProveedores(filtrados);
}


/* ============================================================
   NUEVO
   ============================================================ */

function abrirProveedorNuevo() {

    document
        .getElementById("supplierForm")
        .reset();


    document
        .getElementById("supplierId")
        .value = "";


    document
        .getElementById(
            "supplierModalTitle"
        )
        .textContent =
            "Nuevo proveedor";


    abrirModal("supplierModal");
}


/* ============================================================
   EDITAR
   ============================================================ */

function editarProveedor(id) {

    const proveedor =
        proveedoresActuales.find(
            item =>
                Number(item.id) === Number(id)
        );


    if (!proveedor) {
        return;
    }


    document
        .getElementById("supplierId")
        .value =
            proveedor.id;


    document
        .getElementById("supplierName")
        .value =
            proveedor.nombre || "";


    document
        .getElementById("supplierContact")
        .value =
            proveedor.contacto || "";


    document
        .getElementById(
            "supplierDeliveryDays"
        )
        .value =
            proveedor.diasEntrega;


    document
        .getElementById(
            "supplierModalTitle"
        )
        .textContent =
            "Editar proveedor";


    abrirModal("supplierModal");
}


/* ============================================================
   GUARDAR
   ============================================================ */

async function guardarProveedor(evento) {

    evento.preventDefault();


    if (!esAdmin()) {

        mostrarToast(
            "No tienes permisos para modificar proveedores.",
            "error"
        );

        return;
    }


    const id =
        document
            .getElementById("supplierId")
            .value;


    const proveedor = {

        nombre:
            document
                .getElementById("supplierName")
                .value
                .trim(),

        contacto:
            document
                .getElementById("supplierContact")
                .value
                .trim(),

        diasEntrega:
            Number(
                document
                    .getElementById(
                        "supplierDeliveryDays"
                    )
                    .value
            )
    };


    if (
        proveedor.diasEntrega < 1 ||
        proveedor.diasEntrega > 90
    ) {

        mostrarToast(
            "Los días de entrega deben estar entre 1 y 90.",
            "error"
        );

        return;
    }


    try {

        await peticionApi(
            id
                ? API.RUTAS.PROVEEDORES + "/" + id
                : API.RUTAS.PROVEEDORES,
            {
                method: id ? "PUT" : "POST",
                body: JSON.stringify(proveedor)
            }
        );


        cerrarModal("supplierModal");


        mostrarToast(
            id
                ? "Proveedor actualizado correctamente."
                : "Proveedor creado correctamente.",
            "success"
        );


        await cargarProveedores();

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

async function eliminarProveedor(id) {

    if (!esAdmin()) {

        mostrarToast(
            "No tienes permisos para eliminar proveedores.",
            "error"
        );

        return;
    }


    const proveedor =
        proveedoresActuales.find(
            item =>
                Number(item.id) === Number(id)
        );


    if (!proveedor) {
        return;
    }


    const confirmado =
        window.confirm(
            "¿Deseas eliminar el proveedor \"" +
            proveedor.nombre +
            "\"?\n\n" +
            "Si está asociado a productos u órdenes, " +
            "el backend impedirá eliminarlo."
        );


    if (!confirmado) {
        return;
    }


    try {

        await peticionApi(
            API.RUTAS.PROVEEDORES + "/" + id,
            {
                method: "DELETE"
            }
        );


        mostrarToast(
            "Proveedor eliminado.",
            "success"
        );


        await cargarProveedores();

    } catch (error) {

        if (error.status === 409) {

            mostrarToast(
                "El proveedor posee información histórica o relaciones activas y no puede eliminarse.",
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
