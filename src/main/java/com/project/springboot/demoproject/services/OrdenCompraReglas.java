package com.project.springboot.demoproject.services;

import java.util.Set;

public final class OrdenCompraReglas {

    private OrdenCompraReglas() {}

    public static boolean cantidadValida(int cantidad) {
        return cantidad > 0;
    }

    public static boolean puedeTransicionar(String actual, String nuevo) {

        if (actual == null || nuevo == null) {
            return false;
        }

        return switch (actual) {
            case "BORRADOR" ->
                Set.of("APROBADA", "CANCELADA").contains(nuevo);

            case "APROBADA" ->
                Set.of("RECIBIDA", "CANCELADA").contains(nuevo);

            case "RECIBIDA", "CANCELADA" -> false;

            default -> false;
        };
    }

    public static boolean requiereMovimientoEntrada(
            String actual,
            String nuevo) {

        return "APROBADA".equals(actual)
                && "RECIBIDA".equals(nuevo);
    }

    public static boolean rolPuedeCambiarEstado(
            String rol,
            String actual,
            String nuevo) {

        if ("AGENTE".equals(rol)) {
            return false;
        }

        if (!"ADMIN".equals(rol)
                && !"SUPERADMIN".equals(rol)) {
            return false;
        }

        return puedeTransicionar(actual, nuevo);
    }
}
