package com.project.springboot.demoproject.logitrack;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class LogiTrackIqRedTests {

    private static final String BASE =
            "com.project.springboot.demoproject.services.";

    private Object invokeStatic(
            String className,
            String methodName,
            Class<?>[] parameterTypes,
            Object... args) {

        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName, parameterTypes);
            return method.invoke(null, args);
        } catch (Exception e) {
            fail("Contrato aún no implementado: "
                    + className + "." + methodName
                    + " -> " + e.getClass().getSimpleName());
            return null;
        }
    }

    @Test
    void consumoCeroDebeGenerarCoberturaNula() {
        Object resultado = invokeStatic(
                BASE + "InventarioReglas",
                "calcularDiasCobertura",
                new Class<?>[]{double.class, double.class},
                25.0,
                0.0
        );

        assertNull(resultado);
    }

    @Test
    void consumoCeroDebeGenerarEstadoSinConsumo() {
        Object resultado = invokeStatic(
                BASE + "InventarioReglas",
                "estadoCobertura",
                new Class<?>[]{double.class},
                0.0
        );

        assertEquals("SIN_CONSUMO", String.valueOf(resultado));
    }

    @Test
    void stockIgualPuntoReordenNoDebeEstarEnRiesgo() {
        Object resultado = invokeStatic(
                BASE + "InventarioReglas",
                "estaEnRiesgo",
                new Class<?>[]{double.class, double.class},
                30.0,
                30.0
        );

        assertEquals(Boolean.FALSE, resultado);
    }

    @Test
    void cantidadCeroONegativaDebeSerInvalida() {
        Object cero = invokeStatic(
                BASE + "OrdenCompraReglas",
                "cantidadValida",
                new Class<?>[]{int.class},
                0
        );

        Object negativa = invokeStatic(
                BASE + "OrdenCompraReglas",
                "cantidadValida",
                new Class<?>[]{int.class},
                -5
        );

        assertEquals(Boolean.FALSE, cero);
        assertEquals(Boolean.FALSE, negativa);
    }

    @Test
    void ordenCanceladaNoPuedeAprobarse() {
        Object resultado = invokeStatic(
                BASE + "OrdenCompraReglas",
                "puedeTransicionar",
                new Class<?>[]{String.class, String.class},
                "CANCELADA",
                "APROBADA"
        );

        assertEquals(Boolean.FALSE, resultado);
    }

    @Test
    void aprobadaARecibidaDebeRequerirMovimientoEntrada() {
        Object resultado = invokeStatic(
                BASE + "OrdenCompraReglas",
                "requiereMovimientoEntrada",
                new Class<?>[]{String.class, String.class},
                "APROBADA",
                "RECIBIDA"
        );

        assertEquals(Boolean.TRUE, resultado);
    }

    @Test
    void agenteNoPuedeAprobarOrden() {
        Object resultado = invokeStatic(
                BASE + "OrdenCompraReglas",
                "rolPuedeCambiarEstado",
                new Class<?>[]{
                        String.class,
                        String.class,
                        String.class
                },
                "AGENTE",
                "BORRADOR",
                "APROBADA"
        );

        assertEquals(Boolean.FALSE, resultado);
    }

    @Test
    void severidadInvalidaDebeRechazarse() {
        Object resultado = invokeStatic(
                BASE + "ResumenPanelReglas",
                "severidadValida",
                new Class<?>[]{String.class},
                "CRITICA"
        );

        assertEquals(Boolean.FALSE, resultado);
    }

    @Test
    void pdfBorradorDebeTenerMarcaYSerInvalidadoAlCambiarEstado() {

        Object marca = invokeStatic(
                BASE + "OrdenPdfReglas",
                "requiereMarcaBorrador",
                new Class<?>[]{String.class},
                "BORRADOR"
        );

        Object invalidar = invokeStatic(
                BASE + "OrdenPdfReglas",
                "debeInvalidarseAlCambiarEstado",
                new Class<?>[]{String.class, String.class},
                "BORRADOR",
                "APROBADA"
        );

        assertEquals(Boolean.TRUE, marca);
        assertEquals(Boolean.TRUE, invalidar);
    }
}
