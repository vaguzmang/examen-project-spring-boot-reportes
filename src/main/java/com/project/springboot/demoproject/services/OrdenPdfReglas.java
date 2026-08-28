package com.project.springboot.demoproject.services;

public final class OrdenPdfReglas {

    private OrdenPdfReglas() {}

    public static boolean requiereMarcaBorrador(String estado) {
        return "BORRADOR".equals(estado);
    }

    public static boolean debeInvalidarseAlCambiarEstado(
            String estadoActual,
            String nuevoEstado) {

        if (estadoActual == null || nuevoEstado == null) {
            return false;
        }

        return !estadoActual.equals(nuevoEstado);
    }
}
