package com.project.springboot.demoproject.examen.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteAuditoriaDto {

    private Long auditoriaId;

    private LocalDateTime fechaCambio;

    private String entidad;

    private Long entidadId;

    private String usuario;

    private String campoModificado;

    private String valoresAnteriores;

    private String valoresNuevos;
}