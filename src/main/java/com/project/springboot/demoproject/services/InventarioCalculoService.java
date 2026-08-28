package com.project.springboot.demoproject.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.BodegaCriticaResponse;
import com.project.springboot.demoproject.dto.ProductoRiesgoResponse;
import com.project.springboot.demoproject.dto.StockProductoResponse;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.Movimiento;
import com.project.springboot.demoproject.entities.MovimientoDetalle;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioCalculoService {

    private static final ZoneId BOGOTA =
            ZoneId.of("America/Bogota");

    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;

    @Transactional(readOnly = true)
    public StockProductoResponse consultarStock(Long productoId) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() ->
                        ResourceNotFoundException.of(
                                "Producto", productoId));

        Map<Long, Integer> stock =
                stockPorBodega(productoId);

        List<StockProductoResponse.StockBodega> bodegas =
                bodegaRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Bodega::getId))
                .map(b -> StockProductoResponse.StockBodega.builder()
                        .bodegaId(b.getId())
                        .nombreBodega(b.getNombre())
                        .stock(stock.getOrDefault(b.getId(), 0))
                        .build())
                .toList();

        int total = bodegas.stream()
                .mapToInt(StockProductoResponse.StockBodega::getStock)
                .sum();

        return StockProductoResponse.builder()
                .productoId(producto.getId())
                .nombreProducto(producto.getNombre())
                .precio(producto.getPrecio())
                .stockTotal(total)
                .bodegas(bodegas)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> stockPorBodega(Long productoId) {

        Map<Long, Integer> stock = new HashMap<>();

        for (Bodega bodega : bodegaRepository.findAll()) {
            stock.put(bodega.getId(), 0);
        }

        for (Movimiento movimiento :
                movimientoRepository.findAllConDetalles()) {

            for (MovimientoDetalle detalle :
                    movimiento.getDetalles()) {

                if (!detalle.getProducto().getId()
                        .equals(productoId)) {
                    continue;
                }

                int cantidad = detalle.getCantidad();

                switch (movimiento.getTipo()) {

                    case ENTRADA -> {
                        if (movimiento.getBodegaDestino() != null) {
                            sumar(
                                stock,
                                movimiento.getBodegaDestino().getId(),
                                cantidad
                            );
                        }
                    }

                    case SALIDA -> {
                        if (movimiento.getBodegaOrigen() != null) {
                            sumar(
                                stock,
                                movimiento.getBodegaOrigen().getId(),
                                -cantidad
                            );
                        }
                    }

                    case TRANSFERENCIA -> {
                        if (movimiento.getBodegaOrigen() != null) {
                            sumar(
                                stock,
                                movimiento.getBodegaOrigen().getId(),
                                -cantidad
                            );
                        }

                        if (movimiento.getBodegaDestino() != null) {
                            sumar(
                                stock,
                                movimiento.getBodegaDestino().getId(),
                                cantidad
                            );
                        }
                    }
                }
            }
        }

        return stock;
    }

    @Transactional(readOnly = true)
    public int stockTotal(Long productoId) {
        return stockPorBodega(productoId)
                .values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    @Transactional(readOnly = true)
    public double consumoDiarioPromedio(Long productoId) {

        LocalDate hoy = LocalDate.now(BOGOTA);

        LocalDateTime inicio =
                hoy.minusDays(29).atStartOfDay();

        LocalDateTime fin =
                hoy.plusDays(1).atStartOfDay();

        long totalSalidas = 0;

        for (Movimiento movimiento :
                movimientoRepository.findAllConDetalles()) {

            if (movimiento.getTipo() != TipoMovimiento.SALIDA) {
                continue;
            }

            if (movimiento.getFecha().isBefore(inicio)
                    || !movimiento.getFecha().isBefore(fin)) {
                continue;
            }

            for (MovimientoDetalle detalle :
                    movimiento.getDetalles()) {

                if (detalle.getProducto().getId()
                        .equals(productoId)) {

                    totalSalidas += detalle.getCantidad();
                }
            }
        }

        return totalSalidas / 30.0;
    }

    @Transactional(readOnly = true)
    public List<ProductoRiesgoResponse> productosEnRiesgo() {

        List<ProductoRiesgoResponse> resultado =
                new ArrayList<>();

        for (Producto producto :
                productoRepository.findAll()) {

            if (producto.getProveedorPrincipal() == null) {
                continue;
            }

            int stockTotal = stockTotal(producto.getId());

            double consumo =
                    consumoDiarioPromedio(producto.getId());

            double puntoReorden =
                    InventarioReglas.calcularPuntoReorden(
                            consumo,
                            producto.getProveedorPrincipal()
                                    .getDiasEntrega());

            if (!InventarioReglas.estaEnRiesgo(
                    stockTotal, puntoReorden)) {
                continue;
            }

            Double cobertura =
                    InventarioReglas.calcularDiasCobertura(
                            stockTotal, consumo);

            Map<Long, Integer> porBodega =
                    stockPorBodega(producto.getId());

            Long bodegaDestino =
                    porBodega.entrySet()
                    .stream()
                    .min(
                        Comparator
                        .comparingInt(
                            (Map.Entry<Long, Integer> e) ->
                                e.getValue()
                        )
                        .thenComparingLong(Map.Entry::getKey)
                    )
                    .map(Map.Entry::getKey)
                    .orElse(null);

            resultado.add(
                ProductoRiesgoResponse.builder()
                    .productoId(producto.getId())
                    .nombreProducto(producto.getNombre())
                    .proveedorId(
                        producto.getProveedorPrincipal().getId())
                    .stockTotal(stockTotal)
                    .consumoDiarioPromedio(consumo)
                    .puntoReorden(puntoReorden)
                    .diasCobertura(cobertura)
                    .estadoCobertura(
                        InventarioReglas.estadoCobertura(consumo))
                    .bodegaDestinoId(bodegaDestino)
                    .build()
            );
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<BodegaCriticaResponse> ocupacionBodegas() {

        List<Producto> productos =
                productoRepository.findAll();

        List<BodegaCriticaResponse> resultado =
                new ArrayList<>();

        for (Bodega bodega : bodegaRepository.findAll()) {

            int unidades = 0;

            for (Producto producto : productos) {
                unidades += stockPorBodega(producto.getId())
                        .getOrDefault(bodega.getId(), 0);
            }

            double ocupacion =
                    bodega.getCapacidad() == 0
                    ? 0.0
                    : (unidades * 100.0)
                        / bodega.getCapacidad();

            resultado.add(
                BodegaCriticaResponse.builder()
                    .bodegaId(bodega.getId())
                    .nombreBodega(bodega.getNombre())
                    .unidades(unidades)
                    .capacidad(bodega.getCapacidad())
                    .ocupacion(ocupacion)
                    .build()
            );
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<BodegaCriticaResponse> bodegasCriticas() {
        return ocupacionBodegas()
                .stream()
                .filter(b -> b.getOcupacion() >= 90.0)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> movimientosAyer() {

        LocalDate hoy = LocalDate.now(BOGOTA);

        LocalDateTime inicio =
                hoy.minusDays(1).atStartOfDay();

        LocalDateTime fin =
                hoy.atStartOfDay();

        long entradas = 0;
        long salidas = 0;
        long transferencias = 0;

        for (Movimiento m :
                movimientoRepository.findAllConDetalles()) {

            if (m.getFecha().isBefore(inicio)
                    || !m.getFecha().isBefore(fin)) {
                continue;
            }

            switch (m.getTipo()) {
                case ENTRADA -> entradas++;
                case SALIDA -> salidas++;
                case TRANSFERENCIA -> transferencias++;
            }
        }

        Map<String, Long> resultado =
                new HashMap<>();

        resultado.put("ENTRADA", entradas);
        resultado.put("SALIDA", salidas);
        resultado.put("TRANSFERENCIA", transferencias);

        return resultado;
    }

    private void sumar(
            Map<Long, Integer> mapa,
            Long bodegaId,
            int cantidad) {

        mapa.merge(
                bodegaId,
                cantidad,
                Integer::sum);
    }
}
