import { createServer } from "node:http";
import { createMcpHandler, McpServer } from "@modelcontextprotocol/server";
import { toNodeHandler } from "@modelcontextprotocol/node";
import { z } from "zod/v4";

const API_BASE = process.env.LOGITRACK_API_URL ?? "http://localhost:8080";
const MCP_HOST = process.env.MCP_HOST ?? "127.0.0.1";
const MCP_PORT = Number(process.env.MCP_PORT ?? 3001);
const MCP_AUTH_TOKEN = process.env.MCP_AUTH_TOKEN ?? "";

const AGENTE_USERNAME = process.env.AGENTE_USERNAME;
const AGENTE_PASSWORD = process.env.AGENTE_PASSWORD;

let jwt = null;

/*
 * El MCP nunca accede directamente a MySQL.
 * Toda operación pasa por la API REST de Spring Boot.
 */
async function loginAgente() {
    if (!AGENTE_USERNAME || !AGENTE_PASSWORD) {
        throw new Error(
            "Faltan AGENTE_USERNAME y AGENTE_PASSWORD"
        );
    }

    const response = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: AGENTE_USERNAME,
            password: AGENTE_PASSWORD
        })
    });

    const body = await leerRespuesta(response);

    if (!response.ok) {
        throw new Error(
            `Login AGENTE falló HTTP ${response.status}: ${JSON.stringify(body)}`
        );
    }

    if (body.rol !== "AGENTE") {
        throw new Error(
            `El usuario configurado debe tener rol AGENTE; rol recibido: ${body.rol}`
        );
    }

    jwt = body.token;
    return jwt;
}

async function leerRespuesta(response) {
    const text = await response.text();

    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

async function apiRequest(path, options = {}, reintentar = true) {
    if (!jwt) {
        await loginAgente();
    }

    const headers = {
        Accept: "application/json",
        Authorization: `Bearer ${jwt}`,
        ...(options.body
            ? { "Content-Type": "application/json" }
            : {}),
        ...(options.headers ?? {})
    };

    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers
    });

    /*
     * Si el JWT expiró, iniciar sesión nuevamente
     * y repetir la petición una sola vez.
     */
    if (response.status === 401 && reintentar) {
        jwt = null;
        await loginAgente();
        return apiRequest(path, options, false);
    }

    const body = await leerRespuesta(response);

    if (!response.ok) {
        throw new Error(
            `LogiTrack API HTTP ${response.status}: ${JSON.stringify(body)}`
        );
    }

    return body;
}

function resultado(data) {
    return {
        content: [
            {
                type: "text",
                text: JSON.stringify(data, null, 2)
            }
        ]
    };
}

function errorResultado(error) {
    return {
        isError: true,
        content: [
            {
                type: "text",
                text: error instanceof Error
                    ? error.message
                    : String(error)
            }
        ]
    };
}

function buildServer() {
    const server = new McpServer({
        name: "logitrack-iq-mcp",
        version: "1.0.0"
    });

    // =====================================================
    // 1. consultar_stock_producto
    // =====================================================
    server.registerTool(
        "consultar_stock_producto",
        {
            description:
                "Consulta el stock real de un producto calculado desde movimientos.",
            inputSchema: z.object({
                productoId: z.number().int()
            })
        },
        async ({ productoId }) => {
            try {
                return resultado(
                    await apiRequest(
                        `/productos/${productoId}/stock`
                    )
                );
            } catch (error) {
                return errorResultado(error);
            }
        }
    );

    // =====================================================
    // 2. consultar_bodegas_criticas
    // =====================================================
    server.registerTool(
        "consultar_bodegas_criticas",
        {
            description:
                "Consulta las bodegas cuya ocupación es crítica.",
            inputSchema: z.object({})
        },
        async () => {
            try {
                return resultado(
                    await apiRequest("/bodegas/criticas")
                );
            } catch (error) {
                return errorResultado(error);
            }
        }
    );

    // =====================================================
    // 3. consultar_productos_en_riesgo
    // =====================================================
    server.registerTool(
        "consultar_productos_en_riesgo",
        {
            description:
                "Consulta productos cuyo stock está por debajo del punto de reorden.",
            inputSchema: z.object({})
        },
        async () => {
            try {
                return resultado(
                    await apiRequest("/productos/riesgo")
                );
            } catch (error) {
                return errorResultado(error);
            }
        }
    );

    // =====================================================
    // 4. consultar_kpis
    // =====================================================
    server.registerTool(
        "consultar_kpis",
        {
            description:
                "Consulta los indicadores principales de inventario de LogiTrack IQ.",
            inputSchema: z.object({})
        },
        async () => {
            try {
                return resultado(
                    await apiRequest("/kpis")
                );
            } catch (error) {
                return errorResultado(error);
            }
        }
    );

    // =====================================================
    // 5. crear_orden_borrador
    // =====================================================
    server.registerTool(
        "crear_orden_borrador",
        {
            description:
                "Crea una orden de compra en estado BORRADOR. No puede aprobarla.",
            inputSchema: z.object({
                productoId: z.number().int(),
                proveedorId: z.number().int(),
                bodegaDestinoId: z.number().int(),
                cantidad: z.number().int(),
                precioUnitario: z.number()
            })
        },
        async ({
            productoId,
            proveedorId,
            bodegaDestinoId,
            cantidad,
            precioUnitario
        }) => {
            try {
                return resultado(
                    await apiRequest("/ordenes", {
                        method: "POST",
                        body: JSON.stringify({
                            productoId,
                            proveedorId,
                            bodegaDestinoId,
                            cantidad,
                            precioUnitario
                        })
                    })
                );
            } catch (error) {
                return errorResultado(error);
            }
        }
    );

    // =====================================================
    // 6. publicar_resumen
    // =====================================================
    server.registerTool(
        "publicar_resumen",
        {
            description:
                "Publica el resumen diario generado para el panel de LogiTrack IQ.",
            inputSchema: z.object({
                resumen: z.record(
                    z.string(),
                    z.unknown()
                )
            })
        },
        async ({ resumen }) => {
            try {
                return resultado(
                    await apiRequest("/panel/resumen", {
                        method: "POST",
                        body: JSON.stringify(resumen)
                    })
                );
            } catch (error) {
                return errorResultado(error);
            }
        }
    );

    return server;
}

const handler = createMcpHandler(buildServer);
const nodeHandler = toNodeHandler(handler);

const httpServer = createServer((req, res) => {
    const url = new URL(
        req.url ?? "/",
        `http://${req.headers.host ?? "localhost"}`
    );

    /*
     * Endpoint simple para Docker/healthcheck.
     */
    if (url.pathname === "/health") {
        res.writeHead(200, {
            "Content-Type": "application/json"
        });

        res.end(
            JSON.stringify({
                status: "UP",
                service: "logitrack-iq-mcp"
            })
        );
        return;
    }

    if (url.pathname !== "/mcp") {
        res.writeHead(404, {
            "Content-Type": "application/json"
        });

        res.end(
            JSON.stringify({
                error: "Not Found"
            })
        );
        return;
    }

    /*
     * Protección opcional del endpoint MCP.
     * En Docker configuraremos MCP_AUTH_TOKEN.
     */
    if (MCP_AUTH_TOKEN) {
        const authorization = req.headers.authorization;

        if (
            authorization !==
            `Bearer ${MCP_AUTH_TOKEN}`
        ) {
            res.writeHead(401, {
                "Content-Type": "application/json"
            });

            res.end(
                JSON.stringify({
                    error: "Unauthorized"
                })
            );
            return;
        }
    }

    void nodeHandler(req, res);
});

httpServer.listen(MCP_PORT, MCP_HOST, () => {
    console.log(
        `LogiTrack IQ MCP escuchando en http://${MCP_HOST}:${MCP_PORT}/mcp`
    );
    console.log(
        `API REST: ${API_BASE}`
    );
});

async function shutdown() {
    await handler.close();

    httpServer.close(() => {
        process.exit(0);
    });
}

process.on("SIGINT", shutdown);
process.on("SIGTERM", shutdown);
