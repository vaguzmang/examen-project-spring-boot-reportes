package com.project.springboot.demoproject.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
public class ResumenPanelRequest {

    private LocalDate fecha;
    private String narrativa;
    private List<Alerta> alertas;
    private List<AccionSugerida> accionesSugeridas;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Alerta {
        private String mensaje;
        private String severidad;
        private Long ordenId;
        private Long productoId;
        private Long bodegaId;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccionSugerida {
        private String tipo;
        private String descripcion;
        private Long ordenId;
        private Long productoId;
        private Long bodegaId;
    }
}
