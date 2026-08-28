package com.project.springboot.demoproject.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KpiResponse {

    private Integer productosQuiebre;
    private Integer productosRiesgo;

    private Long ordenesBorrador;
    private BigDecimal totalOrdenesBorrador;

    private Map<String, Long> movimientosAyer;

    private List<BodegaCriticaResponse> ocupacionBodegas;
}
