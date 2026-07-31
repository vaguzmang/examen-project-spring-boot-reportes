package com.project.springboot.demoproject.examen.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.entities.Auditoria;
import com.project.springboot.demoproject.entities.Movimiento;
import com.project.springboot.demoproject.entities.MovimientoDetalle;
import com.project.springboot.demoproject.examen.dto.ReporteAuditoriaDto;
import com.project.springboot.demoproject.examen.dto.ReporteMovimientoDto;
import com.project.springboot.demoproject.repositories.AuditoriaRepository;
import com.project.springboot.demoproject.repositories.MovimientoRepository;
import com.project.springboot.demoproject.enums.TipoMovimiento;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final MovimientoRepository movimientoRepository;
    private final AuditoriaRepository auditoriaRepository;

    @Transactional(readOnly = true)
    public List<ReporteMovimientoDto> obtenerReporteMovimientos(
            Long bodegaId,
            Long productoId,
            TipoMovimiento tipoMovimiento,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        List<Movimiento> movimientos = movimientoRepository.obtenerMovimientosReporte(
                bodegaId,
                productoId,
                tipoMovimiento,
                fechaInicio,
                fechaFin);

        List<ReporteMovimientoDto> respuesta = new ArrayList<>();

        for (Movimiento movimiento : movimientos) {

            for (MovimientoDetalle detalle : movimiento.getDetalles()) {

                ReporteMovimientoDto dto = new ReporteMovimientoDto();

                dto.setMovimientoId(movimiento.getId());
                dto.setFecha(movimiento.getFecha());
                dto.setTipoMovimiento(movimiento.getTipo());

                dto.setProductoId(detalle.getProducto().getId());
                dto.setProducto(detalle.getProducto().getNombre());
                dto.setCantidad(detalle.getCantidad());

                dto.setBodegaOrigen(
                        movimiento.getBodegaOrigen() != null
                                ? movimiento.getBodegaOrigen().getNombre()
                                : null);

                dto.setBodegaDestino(
                        movimiento.getBodegaDestino() != null
                                ? movimiento.getBodegaDestino().getNombre()
                                : null);

                respuesta.add(dto);
            }
        }

        return respuesta;
    }

    @Transactional(readOnly = true)
    public List<ReporteAuditoriaDto> obtenerReporteAuditoria(
            Long productoId,
            String campoModificado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        List<Auditoria> auditorias = auditoriaRepository.obtenerAuditoriaReporte(
                productoId,
                campoModificado,
                fechaInicio,
                fechaFin);

        List<ReporteAuditoriaDto> respuesta = new ArrayList<>();

        for (Auditoria auditoria : auditorias) {

            ReporteAuditoriaDto dto = new ReporteAuditoriaDto();

            dto.setAuditoriaId(auditoria.getId());
            dto.setFechaCambio(auditoria.getFechaHora());
            dto.setEntidad(auditoria.getEntidadAfectada());
            dto.setEntidadId(auditoria.getEntidadId());

            dto.setUsuario(
                    auditoria.getUsuario() != null
                            ? auditoria.getUsuario().getNombre()
                            : null);

            dto.setCampoModificado(auditoria.getCampoModificado());
            dto.setValoresAnteriores(auditoria.getValoresAnteriores());
            dto.setValoresNuevos(auditoria.getValoresNuevos());

            respuesta.add(dto);
        }

        return respuesta;
    }

}