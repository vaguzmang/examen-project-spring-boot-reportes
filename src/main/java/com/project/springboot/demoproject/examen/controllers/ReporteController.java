package com.project.springboot.demoproject.examen.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.examen.dto.ReporteAuditoriaDto;
import com.project.springboot.demoproject.examen.dto.ReporteMovimientoDto;
import com.project.springboot.demoproject.examen.services.ReporteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Reportes consolidados de movimientos y auditoría")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/movimientos")
    @Operation(summary = "Reporte de movimientos con filtros opcionales")
    public List<ReporteMovimientoDto> obtenerReporteMovimientos(

            @RequestParam(required = false)
            Long bodega,

            @RequestParam(required = false)
            Long producto,

            @RequestParam(required = false)
            TipoMovimiento tipoMovimiento,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaFin) {

        return reporteService.obtenerReporteMovimientos(
                bodega,
                producto,
                tipoMovimiento,
                fechaInicio,
                fechaFin);
    }

    @GetMapping("/auditoria")
    @Operation(summary = "Reporte de auditoría con filtros opcionales")
    public List<ReporteAuditoriaDto> obtenerReporteAuditoria(

            @RequestParam(required = false)
            Long producto,

            @RequestParam(required = false)
            String campoModificado,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaFin) {

        return reporteService.obtenerReporteAuditoria(
                producto,
                campoModificado,
                fechaInicio,
                fechaFin);
    }

}