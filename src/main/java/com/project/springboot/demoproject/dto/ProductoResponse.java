package com.project.springboot.demoproject.dto;

import java.math.BigDecimal;

import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.entities.Proveedor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {

    private Long id;
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private Integer stockTotal;

    private Long proveedorPrincipalId;
    private String nombreProveedorPrincipal;

    public static ProductoResponse desde(Producto p, Integer stockTotal) {
        Proveedor proveedor = p.getProveedorPrincipal();

        return new ProductoResponse(
                p.getId(),
                p.getNombre(),
                p.getCategoria(),
                p.getPrecio(),
                stockTotal,
                proveedor != null ? proveedor.getId() : null,
                proveedor != null ? proveedor.getNombre() : null
        );
    }
}
