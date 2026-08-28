package com.project.springboot.demoproject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductoRiesgoResponse {

    private Long productoId;
    private String nombreProducto;
    private Long proveedorId;
    private Integer stockTotal;
    private Double consumoDiarioPromedio;
    private Double puntoReorden;
    private Double diasCobertura;
    private String estadoCobertura;
    private Long bodegaDestinoId;
}
