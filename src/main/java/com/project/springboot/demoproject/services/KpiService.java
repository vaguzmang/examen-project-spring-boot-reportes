package com.project.springboot.demoproject.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.KpiResponse;
import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.enums.EstadoOrdenCompra;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KpiService {

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

        return KpiResponse.builder()
                .productosQuiebre(quiebre)
                .productosRiesgo(riesgo)
                .ordenesBorrador((long) borradores.size())
                .totalOrdenesBorrador(total)
                .movimientosAyer(
                    inventarioCalculoService
                        .movimientosAyer())
                .ocupacionBodegas(
                    inventarioCalculoService
                        .ocupacionBodegas())
                .build();
    }
}
