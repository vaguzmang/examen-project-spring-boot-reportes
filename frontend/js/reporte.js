async function iniciarReportes() {
    document.getElementById("printReportButton").addEventListener("click", () => window.print());
    try {
        const resumen = await peticionApi(API.RUTAS.REPORTES + "/resumen");
        if (!esModuloVigente("reporte")) {
            return;
        }
        construirKpisReporte(resumen);
        construirBarrasReporte(resumen);
        construirProductosMovidos(resumen);
    } catch (error) {
        if (!esModuloVigente("reporte")) {
            return;
        }
        mostrarToast(error.message, "error");
    }
}

function construirKpisReporte(resumen) {
    const stockTotal = resumen.stockPorBodega.reduce((suma, item) => suma + Number(item.stockTotal), 0);

    document.getElementById("reportKpis").innerHTML =
        tarjetaKpi("▦", "Stock consolidado", formatoNumero(stockTotal), "unidades en todas las bodegas", "#dcfce7", "#16a34a") +
        tarjetaKpi("▥", "Bodegas", resumen.totalBodegas, "bodegas registradas", "#ede9fe", "#7c3aed") +
        tarjetaKpi("□", "Productos", resumen.totalProductos, "productos en catálogo", "#dbeafe", "#1d4ed8") +
        tarjetaKpi("⇄", "Movimientos", resumen.totalMovimientos, "movimientos registrados", "#fef3c7", "#d97706");
}

function construirBarrasReporte(resumen) {
    const totales = resumen.stockPorBodega.map(item => ({
        nombre: item.bodegaNombre,
        total: Number(item.stockTotal)
    }));
    const maximo = Math.max(...totales.map(item => item.total), 1);
    document.getElementById("reportWarehouseBars").innerHTML = totales.map(item => `
        <div class="warehouse-row">
            <strong>${escaparHtml(item.nombre)}</strong>
            <div class="bar-track">
                <span class="bar-fill" style="width:${(item.total / maximo) * 100}%"></span>
            </div>
            <span>${formatoNumero(item.total)}</span>
        </div>`).join("");
}

function construirProductosMovidos(resumen) {
    const productos = resumen.productosMasMovidos.slice(0, 5);
    if (productos.length === 0) {
        document.getElementById("topProductsList").innerHTML =
            '<div class="empty-state">Todavía no hay movimientos registrados.</div>';
        return;
    }
    document.getElementById("topProductsList").innerHTML = productos.map((producto, index) => `
        <div class="activity-item">
            <span class="activity-icon badge-blue">${index + 1}</span>
            <div class="activity-copy">
                <strong>${escaparHtml(producto.productoNombre)}</strong>
                <small>${formatoNumero(producto.totalMovido)} unidades movidas</small>
            </div>
        </div>`).join("");
}
