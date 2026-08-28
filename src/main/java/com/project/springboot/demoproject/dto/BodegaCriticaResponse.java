package com.project.springboot.demoproject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BodegaCriticaResponse {

    private Long bodegaId;
    private String nombreBodega;
    private Integer unidades;
    private Integer capacidad;
    private Double ocupacion;
}
