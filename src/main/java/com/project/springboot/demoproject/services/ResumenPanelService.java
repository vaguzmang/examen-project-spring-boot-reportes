package com.project.springboot.demoproject.services;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.springboot.demoproject.audit.CurrentUserProvider;
import com.project.springboot.demoproject.dto.ResumenPanelRequest;
import com.project.springboot.demoproject.entities.ResumenPanel;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;
import com.project.springboot.demoproject.repositories.ResumenPanelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumenPanelService {

    private static final ZoneId BOGOTA =
            ZoneId.of("America/Bogota");

    private final ResumenPanelRepository resumenPanelRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResumenPanelRequest publicar(ResumenPanelRequest request) {

        validar(request);

        String json;

        try {
            json = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    "No fue posible serializar el resumen");
        }

        LocalDate hoy = LocalDate.now(BOGOTA);

        ResumenPanel resumen =
                resumenPanelRepository.findByFecha(hoy)
                .orElseGet(ResumenPanel::new);

        resumen.setFecha(hoy);
        resumen.setContenidoJson(json);

        resumen.setAutor(
            currentUserProvider.getUsuarioActual()
                .orElseThrow(() ->
                    new BusinessException(
                        "No existe usuario autenticado"))
        );

        resumenPanelRepository.save(resumen);

        return request;
    }

    @Transactional(readOnly = true)
    public ResumenPanelRequest obtenerUltimo() {

        ResumenPanel resumen =
            resumenPanelRepository
                .findTopByOrderByFechaDesc()
                .orElseThrow(() ->
                    new BusinessException(
                        "No existe resumen publicado"));

        try {
            return objectMapper.readValue(
                    resumen.getContenidoJson(),
                    ResumenPanelRequest.class);

        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    "El resumen almacenado no es válido");
        }
    }

    private void validar(ResumenPanelRequest request) {

        if (request == null) {
            throw new BusinessException(
                    "El resumen es obligatorio");
        }

        LocalDate hoy = LocalDate.now(BOGOTA);

        if (!hoy.equals(request.getFecha())) {
            throw new BusinessException(
                    "La fecha debe ser la fecha actual de America/Bogota");
        }

        if (!ResumenPanelReglas.narrativaValida(
                request.getNarrativa())) {
            throw new BusinessException(
                    "La narrativa debe tener entre 20 y 500 caracteres");
        }

        if (request.getAlertas() == null
                || request.getAccionesSugeridas() == null) {
            throw new BusinessException(
                    "alertas y accionesSugeridas deben ser arreglos");
        }

        for (ResumenPanelRequest.Alerta alerta :
                request.getAlertas()) {

            if (!ResumenPanelReglas.severidadValida(
                    alerta.getSeveridad())) {
                throw new BusinessException(
                        "Severidad inválida");
            }

            int ids = contarIds(
                    alerta.getOrdenId(),
                    alerta.getProductoId(),
                    alerta.getBodegaId());

            if (ids < 1) {
                throw new BusinessException(
                        "Cada alerta debe referenciar al menos un ID");
            }

            validarIds(
                    alerta.getOrdenId(),
                    alerta.getProductoId(),
                    alerta.getBodegaId());
        }

        for (ResumenPanelRequest.AccionSugerida accion :
                request.getAccionesSugeridas()) {

            if (!ResumenPanelReglas.tipoAccionValido(
                    accion.getTipo())) {
                throw new BusinessException(
                        "Tipo de acción inválido");
            }

            int ids = contarIds(
                    accion.getOrdenId(),
                    accion.getProductoId(),
                    accion.getBodegaId());

            if (ids != 1) {
                throw new BusinessException(
                        "Cada acción debe referenciar exactamente un ID");
            }

            validarIds(
                    accion.getOrdenId(),
                    accion.getProductoId(),
                    accion.getBodegaId());
        }
    }

    private int contarIds(
            Long ordenId,
            Long productoId,
            Long bodegaId) {

        int total = 0;

        if (ordenId != null) total++;
        if (productoId != null) total++;
        if (bodegaId != null) total++;

        return total;
    }

    private void validarIds(
            Long ordenId,
            Long productoId,
            Long bodegaId) {

        if (ordenId != null
                && !ordenCompraRepository.existsById(ordenId)) {
            throw new BusinessException(
                    "ordenId inexistente: " + ordenId);
        }

        if (productoId != null
                && !productoRepository.existsById(productoId)) {
            throw new BusinessException(
                    "productoId inexistente: " + productoId);
        }

        if (bodegaId != null
                && !bodegaRepository.existsById(bodegaId)) {
            throw new BusinessException(
                    "bodegaId inexistente: " + bodegaId);
        }
    }
}
