package com.project.springboot.demoproject.services;

import java.util.Set;

public final class ResumenPanelReglas {

    private ResumenPanelReglas() {}

    private static final Set<String> SEVERIDADES =
            Set.of("BAJA", "MEDIA", "ALTA");

    private static final Set<String> TIPOS =
            Set.of(
                "REVISAR_ORDEN",
                "REVISAR_PRODUCTO",
                "REVISAR_BODEGA"
            );

    public static boolean severidadValida(String severidad) {
        return severidad != null
                && SEVERIDADES.contains(severidad);
    }

    public static boolean tipoAccionValido(String tipo) {
        return tipo != null
                && TIPOS.contains(tipo);
    }

    public static boolean narrativaValida(String narrativa) {
        if (narrativa == null) {
            return false;
        }

        int longitud = narrativa.trim().length();
        return longitud >= 20 && longitud <= 500;
    }
}
