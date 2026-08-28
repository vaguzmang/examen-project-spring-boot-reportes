/*
 * LogiTrack IQ - configuración central del frontend.
 *
 * Este frontend vive de forma independiente en /frontend y consume
 * exclusivamente la API REST de Spring Boot.
 */

const API = {
    BASE_URL: "http://localhost:8080",

    RUTAS: {
        LOGIN: "/auth/login",
        REGISTRO: "/auth/register",

        USUARIOS: "/usuarios",
        BODEGAS: "/bodegas",
        PRODUCTOS: "/productos",
        INVENTARIO: "/inventario",
        MOVIMIENTOS: "/movimientos",
        AUDITORIAS: "/auditorias",
        REPORTES: "/reportes",

        PROVEEDORES: "/proveedores",
        ORDENES: "/ordenes",

        KPIS: "/kpis",
        PRODUCTOS_RIESGO: "/productos/riesgo",
        BODEGAS_CRITICAS: "/bodegas/criticas",

        PANEL_RESUMEN: "/panel/resumen"
    }
};


/* ============================================================
   SESIÓN
   El enunciado de LogiTrack IQ exige sessionStorage.
   ============================================================ */

const TOKEN_KEY = "logitrack_token";
const USER_KEY = "logitrack_usuario";


function guardarToken(token) {
    sessionStorage.setItem(TOKEN_KEY, token);
}


function obtenerToken() {
    return sessionStorage.getItem(TOKEN_KEY);
}


function eliminarToken() {
    sessionStorage.removeItem(TOKEN_KEY);
}


function guardarUsuario(usuario) {
    sessionStorage.setItem(USER_KEY, JSON.stringify(usuario));
}


function obtenerUsuario() {
    const usuario = sessionStorage.getItem(USER_KEY);

    if (!usuario) {
        return null;
    }

    try {
        return JSON.parse(usuario);
    } catch {
        sessionStorage.removeItem(USER_KEY);
        return null;
    }
}


function eliminarUsuario() {
    sessionStorage.removeItem(USER_KEY);
}


function limpiarSesion() {
    eliminarToken();
    eliminarUsuario();
}


/* ============================================================
   PETICIONES JSON
   ============================================================ */

async function peticionApi(ruta, opciones = {}) {

    const headers = {
        ...(opciones.body ? { "Content-Type": "application/json" } : {}),
        ...(opciones.headers || {})
    };

    const esLogin = ruta === API.RUTAS.LOGIN;
    const token = obtenerToken();

    if (token && !esLogin) {
        headers.Authorization = "Bearer " + token;
    }

    let respuesta;

    try {
        respuesta = await fetch(
            API.BASE_URL + ruta,
            {
                ...opciones,
                headers
            }
        );
    } catch {
        throw new Error(
            "No fue posible conectar con el backend de LogiTrack IQ."
        );
    }


    /*
     * Sesión inválida o vencida.
     */
    if (respuesta.status === 401 && !esLogin) {

        limpiarSesion();

        if (!window.location.pathname.endsWith("login.html")) {
            window.location.href = "login.html";
        }

        throw new Error(
            "La sesión expiró. Inicia sesión nuevamente."
        );
    }


    if (!respuesta.ok) {

        let mensaje =
            `No fue posible completar la solicitud (${respuesta.status}).`;

        try {
            const error = await respuesta.json();

            mensaje =
                error.message ||
                error.error ||
                mensaje;

            if (
                Array.isArray(error.detalles) &&
                error.detalles.length > 0
            ) {
                mensaje += ": " + error.detalles.join("; ");
            }

        } catch {
            /*
             * El backend puede responder sin cuerpo JSON.
             */
        }

        const error = new Error(mensaje);
        error.status = respuesta.status;

        throw error;
    }


    if (respuesta.status === 204) {
        return null;
    }


    const tipoContenido =
        respuesta.headers.get("content-type") || "";


    if (tipoContenido.includes("application/json")) {
        return respuesta.json();
    }


    return respuesta.text();
}


/* ============================================================
   PDF
   ============================================================ */

async function peticionPdf(ruta, opciones = {}) {

    const token = obtenerToken();

    const headers = {
        ...(opciones.headers || {})
    };

    if (token) {
        headers.Authorization = "Bearer " + token;
    }


    const respuesta = await fetch(
        API.BASE_URL + ruta,
        {
            ...opciones,
            headers
        }
    );


    if (respuesta.status === 401) {

        limpiarSesion();
        window.location.href = "login.html";

        throw new Error(
            "La sesión expiró. Inicia sesión nuevamente."
        );
    }


    if (!respuesta.ok) {

        let mensaje =
            `No fue posible obtener el PDF (${respuesta.status}).`;

        try {

            const error = await respuesta.json();

            mensaje =
                error.message ||
                error.error ||
                mensaje;

        } catch {
            // respuesta no JSON
        }

        const error = new Error(mensaje);
        error.status = respuesta.status;

        throw error;
    }


    return respuesta.blob();
}


/* ============================================================
   HELPERS LOGITRACK IQ
   ============================================================ */

function rutaStockProducto(productoId) {
    return `/productos/${productoId}/stock`;
}


function rutaOrdenPdf(ordenId) {
    return `/ordenes/${ordenId}/pdf`;
}


function rutaEstadoOrden(ordenId) {
    return `/ordenes/${ordenId}/estado`;
}


function esAdmin() {
    const usuario = obtenerUsuario();

    return usuario &&
        (
            usuario.rol === "ADMIN" ||
            usuario.rol === "SUPERADMIN"
        );
}


function esAgente() {
    const usuario = obtenerUsuario();

    return usuario &&
        usuario.rol === "AGENTE";
}
