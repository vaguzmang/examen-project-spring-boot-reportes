package com.project.springboot.demoproject.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.enums.EstadoOrdenCompra;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrdenCompraResponse {

    private Long id;

    private Long productoId;
    private String nombreProducto;

    private Long proveedorId;
    private String nombreProveedor;

    private Long bodegaDestinoId;
    private String nombreBodegaDestino;

    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal total;

    private LocalDateTime fechaCreacion;
    private EstadoOrdenCompra estado;

    private String creadoPor;

    private boolean pdfDisponible;
    private LocalDateTime fechaGeneracionPdf;

    public static OrdenCompraResponse desde(OrdenCompra o) {
        return OrdenCompraResponse.builder()
                .id(o.getId())
                .productoId(o.getProducto().getId())
                .nombreProducto(o.getProducto().getNombre())
                .proveedorId(o.getProveedor().getId())
                .nombreProveedor(o.getProveedor().getNombre())
                .bodegaDestinoId(o.getBodegaDestino().getId())
                .nombreBodegaDestino(o.getBodegaDestino().getNombre())
                .cantidad(o.getCantidad())
                .precioUnitario(o.getPrecioUnitario())
                .total(o.getTotal())
                .fechaCreacion(o.getFechaCreacion())
                .estado(o.getEstado())
                .creadoPor(o.getCreadoPor().getUsername())
                .pdfDisponible(o.getPdf() != null)
                .fechaGeneracionPdf(o.getFechaGeneracionPdf())
                .build();
    }
}
