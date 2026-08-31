package com.project.springboot.demoproject.services;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.BodegaCriticaResponse;
import com.project.springboot.demoproject.dto.KpiResponse;
import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.enums.EstadoOrdenCompra;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KpiService {

    private static final ZoneId BOGOTA =
            ZoneId.of("America/Bogota");

    private final InventarioCalculoService inventarioCalculoService;
    private final ProductoRepository productoRepository;
    private final OrdenCompraRepository ordenCompraRepository;

    @Transactional(readOnly = true)
    public KpiResponse obtener() {

        int quiebre = (int) productoRepository.findAll()
                .stream()
                .filter(p ->
                    inventarioCalculoService
                        .stockTotal(p.getId()) == 0)
                .count();

        int riesgo =
                inventarioCalculoService
                    .productosEnRiesgo()
                    .size();

        List<OrdenCompra> borradores =
                ordenCompraRepository
                    .findByEstadoOrderByFechaCreacionDesc(
                        EstadoOrdenCompra.BORRADOR);

        BigDecimal total =
                borradores.stream()
                .map(OrdenCompra::getTotal)
                .reduce(
                    BigDecimal.ZERO,
                    BigDecimal::add);

        List<BodegaCriticaResponse> ocupacion =
                inventarioCalculoService
                    .ocupacionBodegas();

        List<KpiResponse.OcupacionPorBodega> ocupacionCanonica =
                ocupacion.stream()
                .map(b ->
                    KpiResponse.OcupacionPorBodega.builder()
                        .bodegaId(b.getBodegaId())
                        .nombre(b.getNombreBodega())
                        .porcentaje(b.getOcupacion())
                        .build()
                )
                .toList();

        Map<String, Long> movimientosOriginal =
                inventarioCalculoService
                    .movimientosAyer();

        Map<String, Long> movimientos =
                new LinkedHashMap<>();

        long entradas =
                movimientosOriginal.getOrDefault("ENTRADA", 0L);
        long salidas =
                movimientosOriginal.getOrDefault("SALIDA", 0L);
        long transferencias =
                movimientosOriginal.getOrDefault("TRANSFERENCIA", 0L);

        movimientos.put("entrada", entradas);
        movimientos.put("salida", salidas);
        movimientos.put("transferencia", transferencias);
        movimientos.put("ENTRADA", entradas);
        movimientos.put("SALIDA", salidas);
        movimientos.put("TRANSFERENCIA", transferencias);

        return KpiResponse.builder()
                .calculadoEn(OffsetDateTime.now(BOGOTA))
                .ocupacionPorBodega(ocupacionCanonica)
                .productosEnQuiebre(quiebre)
                .productosEnRiesgo(riesgo)
                .ordenesPorAprobar(
                    KpiResponse.OrdenesPorAprobar.builder()
                        .cantidad((long) borradores.size())
                        .montoTotal(total)
                        .build())
                .movimientosAyer(movimientos)
                .productosQuiebre(quiebre)
                .productosRiesgo(riesgo)
                .ordenesBorrador((long) borradores.size())
                .totalOrdenesBorrador(total)
                .ocupacionBodegas(ocupacion)
                .build();
    }
}
