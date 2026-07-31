package com.project.springboot.demoproject.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.reportes.ProductoMasMovidoDto;
import com.project.springboot.demoproject.dto.reportes.ReporteResumenDto;
import com.project.springboot.demoproject.dto.reportes.StockPorBodegaDto;
import com.project.springboot.demoproject.services.ReporteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Consultas avanzadas y reporte resumen general")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/stock-por-bodega")
    @Operation(summary = "Stock total por bodega")
    public List<StockPorBodegaDto> stockPorBodega() {
        return reporteService.stockTotalPorBodega();
    }

    @GetMapping("/productos-mas-movidos")
    @Operation(summary = "Productos mas movidos (opcionalmente en un rango de fechas)")
    public List<ProductoMasMovidoDto> productosMasMovidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        if (desde != null && hasta != null) {
            return reporteService.productosMasMovidosPorFecha(desde, hasta);
        }
        return reporteService.productosMasMovidos();
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen general del sistema: stock por bodega, productos mas movidos y totales")
    public ReporteResumenDto resumen() {
        return reporteService.resumenGeneral();
    }
}
