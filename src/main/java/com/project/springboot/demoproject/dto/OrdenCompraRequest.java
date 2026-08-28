package com.project.springboot.demoproject.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrdenCompraRequest {

    @NotNull
    private Long productoId;

    @NotNull
    private Long proveedorId;

    @NotNull
    private Long bodegaDestinoId;

    @NotNull
    @Min(1)
    private Integer cantidad;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal precioUnitario;
}
