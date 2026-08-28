package com.project.springboot.demoproject.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockProductoResponse {

    private Long productoId;
    private String nombreProducto;
    private Integer stockTotal;
    private List<StockBodega> bodegas;

    @Data
    @Builder
    public static class StockBodega {
        private Long bodegaId;
        private String nombreBodega;
        private Integer stock;
    }
}
