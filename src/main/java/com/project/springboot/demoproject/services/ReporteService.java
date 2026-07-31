package com.project.springboot.demoproject.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.springboot.demoproject.dto.reportes.ProductoMasMovidoDto;
import com.project.springboot.demoproject.dto.reportes.ReporteResumenDto;
import com.project.springboot.demoproject.dto.reportes.StockPorBodegaDto;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.InventarioBodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoDetalleRepository;
import com.project.springboot.demoproject.repositories.MovimientoRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final MovimientoDetalleRepository movimientoDetalleRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;

    public List<StockPorBodegaDto> stockTotalPorBodega() {
        return inventarioBodegaRepository.obtenerStockTotalPorBodega().stream()
                .map(fila -> new StockPorBodegaDto((Long) fila[0], (String) fila[1], ((Number) fila[2]).longValue()))
                .toList();
    }

    public List<ProductoMasMovidoDto> productosMasMovidos() {
        return movimientoDetalleRepository.obtenerProductosMasMovidos().stream()
                .map(fila -> new ProductoMasMovidoDto((Long) fila[0], (String) fila[1], ((Number) fila[2]).longValue()))
                .toList();
    }

    public List<ProductoMasMovidoDto> productosMasMovidosPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoDetalleRepository.obtenerProductosMasMovidosPorFecha(inicio, fin).stream()
                .map(fila -> new ProductoMasMovidoDto((Long) fila[0], (String) fila[1], ((Number) fila[2]).longValue()))
                .toList();
    }

    /** Reporte general resumido en JSON, como pide el punto 6 del enunciado. */
    public ReporteResumenDto resumenGeneral() {
        ReporteResumenDto resumen = new ReporteResumenDto();
        resumen.setStockPorBodega(stockTotalPorBodega());
        resumen.setProductosMasMovidos(productosMasMovidos());
        resumen.setTotalBodegas(bodegaRepository.count());
        resumen.setTotalProductos(productoRepository.count());
        resumen.setTotalMovimientos(movimientoRepository.count());
        return resumen;
    }
}
