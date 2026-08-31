package com.project.springboot.demoproject.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KpiResponse {

    private OffsetDateTime calculadoEn;
    private List<OcupacionPorBodega> ocupacionPorBodega;
    private Integer productosEnQuiebre;
    private Integer productosEnRiesgo;
    private OrdenesPorAprobar ordenesPorAprobar;
    private Map<String, Long> movimientosAyer;

    // Compatibilidad con la primera versión del frontend.
    private Integer productosQuiebre;
    private Integer productosRiesgo;
    private Long ordenesBorrador;
    private BigDecimal totalOrdenesBorrador;
    private List<BodegaCriticaResponse> ocupacionBodegas;

    @Data
    @Builder
    public static class OcupacionPorBodega {
        private Long bodegaId;
        private String nombre;
        private Double porcentaje;
    }

    @Data
    @Builder
    public static class OrdenesPorAprobar {
        private Long cantidad;
        private BigDecimal montoTotal;
    }
}
