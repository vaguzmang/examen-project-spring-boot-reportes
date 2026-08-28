package com.project.springboot.demoproject.dto;

import com.project.springboot.demoproject.enums.EstadoOrdenCompra;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EstadoOrdenCompraRequest {

    @NotNull
    private EstadoOrdenCompra estado;
}
