package com.project.springboot.demoproject.services;

public final class InventarioReglas {

    private InventarioReglas() {}

    public static Double calcularDiasCobertura(double stockTotal, double consumoDiario) {
        if (consumoDiario <= 0) {
            return null;
        }
        return stockTotal / consumoDiario;
    }

    public static String estadoCobertura(double consumoDiario) {
        return consumoDiario <= 0 ? "SIN_CONSUMO" : "CON_CONSUMO";
    }

    public static boolean estaEnRiesgo(double stockTotal, double puntoReorden) {
        return stockTotal < puntoReorden;
    }

    public static double calcularPuntoReorden(
            double consumoDiario,
            int diasEntrega) {
        return consumoDiario * diasEntrega * 1.5;
    }
}
