package com.project.springboot.demoproject.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.springboot.demoproject.dto.EstadoOrdenCompraRequest;
import com.project.springboot.demoproject.dto.OrdenCompraRequest;
import com.project.springboot.demoproject.dto.OrdenCompraResponse;
import com.project.springboot.demoproject.enums.EstadoOrdenCompra;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.services.OrdenCompraService;
import com.project.springboot.demoproject.services.OrdenPdfService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
@Tag(name = "Ordenes de compra")
@SecurityRequirement(name = "bearerAuth")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;
    private final OrdenPdfService ordenPdfService;

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public List<OrdenCompraResponse> listar(
            @RequestParam(required = false)
            EstadoOrdenCompra estado) {

        return ordenCompraService.listar(estado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public OrdenCompraResponse obtener(
            @PathVariable Long id) {

        return ordenCompraService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public ResponseEntity<OrdenCompraResponse> crear(
            @Valid @RequestBody OrdenCompraRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordenCompraService.crearBorrador(request));
    }


    @PostMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public ResponseEntity<byte[]> generarPdf(
            @PathVariable Long id) {

        byte[] pdf = ordenPdfService.generar(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                    "Content-Disposition",
                    "inline; filename=orden-" + id + ".pdf")
                .body(pdf);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public ResponseEntity<byte[]> obtenerPdf(
            @PathVariable Long id) {

        byte[] pdf = ordenPdfService.obtener(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                    "Content-Disposition",
                    "inline; filename=orden-" + id + ".pdf")
                .body(pdf);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public OrdenCompraResponse cambiarEstado(
            @PathVariable Long id,
            @RequestBody JsonNode json) {

        if (json == null
                || !json.isObject()
                || json.size() != 1
                || !json.has("estado")
                || !json.get("estado").isTextual()) {

            throw new BusinessException(
                    "El cuerpo debe contener exactamente el campo estado");
        }

        EstadoOrdenCompra estado;

        try {
            estado = EstadoOrdenCompra.valueOf(
                    json.get("estado").asText());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    "Estado de orden inválido");
        }

        EstadoOrdenCompraRequest request =
                new EstadoOrdenCompraRequest();

        request.setEstado(estado);

        return ordenCompraService.cambiarEstado(id, request);
    }
}
