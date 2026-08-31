package com.project.springboot.demoproject.controllers;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.springboot.demoproject.dto.ResumenPanelRequest;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.services.ResumenPanelService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/panel/resumen")
@RequiredArgsConstructor
@Tag(name = "Resumen Panel LogiTrack IQ")
@SecurityRequirement(name = "bearerAuth")
public class ResumenPanelController {

    private static final Set<String> CAMPOS_RESUMEN =
            Set.of(
                "fecha",
                "narrativa",
                "alertas",
                "accionesSugeridas"
            );

    private static final Set<String> CAMPOS_ALERTA =
            Set.of(
                "severidad",
                "titulo",
                "detalle",
                "productoId",
                "ordenId",
                "bodegaId"
            );

    private static final Set<String> CAMPOS_ACCION =
            Set.of(
                "tipo",
                "descripcion",
                "ordenId",
                "productoId",
                "bodegaId"
            );

    private final ResumenPanelService resumenPanelService;
    private final ObjectMapper objectMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public ResumenPanelRequest publicar(
            @RequestBody JsonNode json) {

        validarEstructuraJson(json);

        try {
            ResumenPanelRequest request =
                    objectMapper.treeToValue(
                            json,
                            ResumenPanelRequest.class);

            return resumenPanelService.publicar(request);

        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    "JSON de resumen inválido");
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public ResumenPanelRequest obtener() {
        return resumenPanelService.obtenerUltimo();
    }

    private void validarEstructuraJson(JsonNode json) {

        if (json == null || !json.isObject()) {
            throw new BusinessException(
                    "El resumen debe ser un objeto JSON");
        }

        if (!campos(json).equals(CAMPOS_RESUMEN)) {
            throw new BusinessException(
                    "El resumen debe contener exactamente: "
                    + "fecha, narrativa, alertas y accionesSugeridas");
        }

        JsonNode alertas = json.get("alertas");
        JsonNode acciones = json.get("accionesSugeridas");

        if (alertas == null || !alertas.isArray()) {
            throw new BusinessException(
                    "alertas debe ser un arreglo");
        }

        if (acciones == null || !acciones.isArray()) {
            throw new BusinessException(
                    "accionesSugeridas debe ser un arreglo");
        }

        for (JsonNode alerta : alertas) {

            if (!alerta.isObject()) {
                throw new BusinessException(
                        "Cada alerta debe ser un objeto JSON");
            }

            if (!CAMPOS_ALERTA.containsAll(campos(alerta))) {
                throw new BusinessException(
                        "La alerta contiene campos no permitidos");
            }

            if (!textoObligatorio(alerta, "severidad")
                    || !textoObligatorio(alerta, "titulo")
                    || !textoObligatorio(alerta, "detalle")) {
                throw new BusinessException(
                        "Cada alerta requiere severidad, titulo y detalle");
            }
        }

        for (JsonNode accion : acciones) {

            if (!accion.isObject()) {
                throw new BusinessException(
                        "Cada acción sugerida debe ser un objeto JSON");
            }

            if (!CAMPOS_ACCION.containsAll(campos(accion))) {
                throw new BusinessException(
                        "La acción sugerida contiene campos no permitidos");
            }

            if (!textoObligatorio(accion, "tipo")
                    || !textoObligatorio(accion, "descripcion")) {
                throw new BusinessException(
                        "Cada acción requiere tipo y descripcion");
            }
        }
    }

    private boolean textoObligatorio(
            JsonNode node,
            String campo) {

        return node.has(campo)
                && node.get(campo).isTextual()
                && !node.get(campo).asText().isBlank();
    }

    private Set<String> campos(JsonNode node) {

        Set<String> nombres = new HashSet<>();
        node.fieldNames().forEachRemaining(nombres::add);

        return nombres;
    }
}
