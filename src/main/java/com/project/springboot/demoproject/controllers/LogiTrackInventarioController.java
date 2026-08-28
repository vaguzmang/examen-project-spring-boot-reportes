package com.project.springboot.demoproject.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.BodegaCriticaResponse;
import com.project.springboot.demoproject.dto.KpiResponse;
import com.project.springboot.demoproject.dto.ProductoRiesgoResponse;
import com.project.springboot.demoproject.dto.StockProductoResponse;
import com.project.springboot.demoproject.services.InventarioCalculoService;
import com.project.springboot.demoproject.services.KpiService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "LogiTrack IQ")
@SecurityRequirement(name = "bearerAuth")
public class LogiTrackInventarioController {

    private final InventarioCalculoService inventarioCalculoService;
    private final KpiService kpiService;

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public KpiResponse kpis() {
        return kpiService.obtener();
    }

    @GetMapping("/productos/{id}/stock")
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public StockProductoResponse stock(
            @PathVariable Long id) {

        return inventarioCalculoService.consultarStock(id);
    }

    @GetMapping("/productos/riesgo")
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public List<ProductoRiesgoResponse> riesgo() {

        return inventarioCalculoService
                .productosEnRiesgo();
    }

    @GetMapping("/bodegas/criticas")
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public List<BodegaCriticaResponse> criticas() {

        return inventarioCalculoService
                .bodegasCriticas();
    }
}
