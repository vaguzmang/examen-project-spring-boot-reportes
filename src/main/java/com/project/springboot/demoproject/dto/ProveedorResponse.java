package com.project.springboot.demoproject.dto;

import com.project.springboot.demoproject.entities.Proveedor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProveedorResponse {

    private Long id;
    private String nombre;
    private String contacto;
    private Integer diasEntrega;

    public static ProveedorResponse desde(Proveedor p) {
        return ProveedorResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .contacto(p.getContacto())
                .diasEntrega(p.getDiasEntrega())
                .build();
    }
}
