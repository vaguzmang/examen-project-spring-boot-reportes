let movimientosActuales = [];
let movimientoBodegas = [];
let movimientoProductos = [];
let contadorFilasProducto = 0;

async function iniciarMovimientos() {
    configurarCierreModal("movementModal");
    movimientoBodegas = await peticionApi(API.RUTAS.BODEGAS);
    movimientoProductos = await peticionApi(API.RUTAS.PRODUCTOS);

    // El usuario pudo haber navegado a otro modulo mientras esperabamos
    // estas dos peticiones; si ya no estamos en "movimiento", el HTML de
    // este fragmento ya no existe en el DOM y no hay nada que inicializar.
    if (!esModuloVigente("movimiento")) {
        return;
    }

    document.getElementById("newMovementButton").addEventListener("click", abrirMovimientoNuevo);
    document.getElementById("movementForm").addEventListener("submit", guardarMovimiento);
    document.getElementById("movementType").addEventListener("change", actualizarCamposMovimiento);
    document.getElementById("movementSearch").addEventListener("input", filtrarMovimientos);
    document.getElementById("movementTypeFilter").addEventListener("change", filtrarMovimientos);
    document.getElementById("movementOrderSort").addEventListener("change", filtrarMovimientos);
    document.getElementById("addProductRowButton").addEventListener("click", () => agregarFilaProducto());
    document.getElementById("applyDateFilterButton").addEventListener("click", cargarMovimientosPorFecha);
    document.getElementById("clearDateFilterButton").addEventListener("click", limpiarFiltroFecha);

    llenarSelectBodegasMovimiento();
    await cargarMovimientos();
}

async function cargarMovimientos() {
    try {
        movimientosActuales = await peticionApi(API.RUTAS.MOVIMIENTOS);
        if (!esModuloVigente("movimiento")) {
            return;
        }
        renderizarMovimientos(movimientosActuales);
    } catch (error) {
        if (!esModuloVigente("movimiento")) {
            return;
        }
        mostrarToast(error.message, "error");
        renderizarMovimientos([]);
    }
}

/** Consulta avanzada: movimientos por rango de fechas (BETWEEN) contra el backend. */
async function cargarMovimientosPorFecha() {
    const desde = document.getElementById("movementDateFrom").value;
    const hasta = document.getElementById("movementDateTo").value;
    if (!desde || !hasta) {
        mostrarToast("Selecciona ambas fechas (desde y hasta).", "error");
        return;
    }
    try {
        const query = `?desde=${desde}T00:00:00&hasta=${hasta}T23:59:59`;
        movimientosActuales = await peticionApi(API.RUTAS.MOVIMIENTOS + query);
        renderizarMovimientos(movimientosActuales);
        mostrarToast("Movimientos filtrados por fecha.", "success");
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}

function limpiarFiltroFecha() {
    document.getElementById("movementDateFrom").value = "";
    document.getElementById("movementDateTo").value = "";
    cargarMovimientos();
}

function llenarSelectBodegasMovimiento() {
    const opciones = '<option value="">Selecciona una bodega</option>' +
        movimientoBodegas.map(item => `<option value="${item.id}">${escaparHtml(item.nombre)}</option>`).join("");
    document.getElementById("movementOrigin").innerHTML = opciones;
    document.getElementById("movementDestination").innerHTML = opciones;
}

function abrirMovimientoNuevo() {
    document.getElementById("movementForm").reset();
    document.getElementById("movementProductRows").innerHTML = "";
    contadorFilasProducto = 0;
    agregarFilaProducto();
    actualizarCamposMovimiento();
    abrirModal("movementModal");
}

/** Agrega una fila de "producto + cantidad" al desglose del movimiento. */
function agregarFilaProducto() {
    contadorFilasProducto += 1;
    const id = contadorFilasProducto;
    const contenedor = document.getElementById("movementProductRows");

    const fila = document.createElement("div");
    fila.className = "product-row";
    fila.dataset.rowId = String(id);
    fila.innerHTML = `
        <select class="row-product" required>
            ${movimientoProductos.map(p => `<option value="${p.id}">${escaparHtml(p.nombre)}</option>`).join("")}
        </select>
        <input class="row-quantity" type="number" min="1" value="1" required placeholder="Cantidad">
        <button type="button" class="product-row-remove" onclick="quitarFilaProducto(${id})" aria-label="Quitar producto">×</button>
    `;
    contenedor.appendChild(fila);
    actualizarBotonesQuitarFila();
}

function quitarFilaProducto(id) {
    const contenedor = document.getElementById("movementProductRows");
    const fila = contenedor.querySelector(`[data-row-id="${id}"]`);
    if (fila) {
        fila.remove();
    }
    actualizarBotonesQuitarFila();
}

function actualizarBotonesQuitarFila() {
    const filas = document.querySelectorAll("#movementProductRows .product-row");
    filas.forEach(fila => {
        const boton = fila.querySelector(".product-row-remove");
        boton.disabled = filas.length === 1;
    });
}

function actualizarCamposMovimiento() {
    const tipo = document.getElementById("movementType").value;
    const origen = document.getElementById("originGroup");
    const destino = document.getElementById("destinationGroup");
    origen.classList.toggle("hidden", tipo === "ENTRADA" || !tipo);
    destino.classList.toggle("hidden", tipo === "SALIDA" || !tipo);
}

function normalizarBusquedaMovimiento(valor) {
    return String(valor ?? "")
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .trim();
}


function filtrarMovimientos() {

    const texto = normalizarBusquedaMovimiento(
        document.getElementById("movementSearch").value
    );

    const tipo =
        document.getElementById("movementTypeFilter").value;


    const filtrados = movimientosActuales.filter(item => {

        /*
         * Incluimos también el detalle de productos.
         * Ejemplo:
         * "Mouse inalambrico Logitech 1"
         */
        const productos = (item.detalles || [])
            .map(detalle => {
                return [
                    detalle.productoNombre,
                    detalle.cantidad
                ].join(" ");
            })
            .join(" ");


        const contenidoBuscable = normalizarBusquedaMovimiento([
            item.id,
            item.tipo,
            item.usuarioUsername,
            item.bodegaOrigenNombre,
            item.bodegaDestinoNombre,
            productos
        ].join(" "));


        const coincideTexto =
            !texto ||
            contenidoBuscable.includes(texto);


        const coincideTipo =
            !tipo ||
            item.tipo === tipo;


        return coincideTexto && coincideTipo;
    });


    renderizarMovimientos(filtrados);
}

function ordenarMovimientos(lista) {
    const selector = document.getElementById("movementOrderSort");
    const direccion = selector ? selector.value : "DESC";

    return lista.slice().sort((a, b) => {
        const fechaA = new Date(a.fecha).getTime();
        const fechaB = new Date(b.fecha).getTime();

        let comparacion;

        if (!Number.isNaN(fechaA) && !Number.isNaN(fechaB)) {
            comparacion = fechaA - fechaB;
        } else {
            comparacion = Number(a.id || 0) - Number(b.id || 0);
        }

        // Si tienen exactamente la misma fecha, usamos ID como desempate.
        if (comparacion === 0) {
            comparacion = Number(a.id || 0) - Number(b.id || 0);
        }

        return direccion === "ASC"
            ? comparacion
            : -comparacion;
    });
}


function renderizarMovimientos(lista) {
    const body = document.getElementById("movementTableBody");
    document.getElementById("movementTableFooter").textContent =
        lista.length + (lista.length === 1 ? " movimiento registrado" : " movimientos registrados");

    if (lista.length === 0) {
        body.innerHTML = '<tr><td colspan="7"><div class="empty-state">No hay movimientos para mostrar.</div></td></tr>';
        return;
    }

    const ordenados = ordenarMovimientos(lista);

    body.innerHTML = ordenados.map(item => {
        const clase = item.tipo === "ENTRADA" ? "badge-green"
            : item.tipo === "SALIDA" ? "badge-red" : "badge-blue";
        const detalle = (item.detalles || [])
            .map(d => `${escaparHtml(d.productoNombre)} (${d.cantidad})`)
            .join(", ");
        return `
            <tr>
                <td>#${item.id}</td>
                <td>${formatoFecha(item.fecha)}</td>
                <td><span class="badge ${clase}">${item.tipo}</span></td>
                <td>${escaparHtml(item.usuarioUsername || "")}</td>
                <td>${escaparHtml(item.bodegaOrigenNombre || "—")}</td>
                <td>${escaparHtml(item.bodegaDestinoNombre || "—")}</td>
                <td>${detalle || "—"}</td>
            </tr>`;
    }).join("");
}

async function guardarMovimiento(evento) {
    evento.preventDefault();
    const tipo = document.getElementById("movementType").value;
    const origen = document.getElementById("movementOrigin").value;
    const destino = document.getElementById("movementDestination").value;

    if (tipo === "TRANSFERENCIA" && origen === destino) {
        mostrarToast("La bodega de origen y destino deben ser diferentes.", "error");
        return;
    }

    const filas = document.querySelectorAll("#movementProductRows .product-row");
    const detalles = Array.from(filas).map(fila => ({
        productoId: Number(fila.querySelector(".row-product").value),
        cantidad: Number(fila.querySelector(".row-quantity").value)
    }));

    const productosRepetidos = new Set(detalles.map(d => d.productoId)).size !== detalles.length;
    if (productosRepetidos) {
        mostrarToast("No repitas el mismo producto en varias filas; ajusta la cantidad en una sola fila.", "error");
        return;
    }

    const movimiento = {
        tipo,
        bodegaOrigenId: tipo === "ENTRADA" ? null : Number(origen),
        bodegaDestinoId: tipo === "SALIDA" ? null : Number(destino),
        detalles
    };

    try {
        await peticionApi(API.RUTAS.MOVIMIENTOS, {
            method: "POST",
            body: JSON.stringify(movimiento)
        });
        cerrarModal("movementModal");
        mostrarToast("Movimiento registrado correctamente.", "success");
        await cargarMovimientos();
    } catch (error) {
        mostrarToast(error.message, "error");
    }
}
