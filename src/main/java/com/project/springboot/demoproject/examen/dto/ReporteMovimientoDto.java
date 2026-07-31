package com.project.springboot.demoproject.examen.dto;

import java.time.LocalDateTime;

import com.project.springboot.demoproject.enums.TipoMovimiento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteMovimientoDto {

    private Long movimientoId;

    private LocalDateTime fecha;

    private TipoMovimiento tipoMovimiento;

    private Long productoId;

    private String producto;

    private Integer cantidad;

    private String bodegaOrigen;

    private String bodegaDestino;
}