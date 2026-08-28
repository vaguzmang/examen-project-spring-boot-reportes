package com.project.springboot.demoproject.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProveedorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @Size(max = 200, message = "El contacto no puede superar 200 caracteres")
    private String contacto;

    @Min(value = 1, message = "Los dias de entrega deben ser minimo 1")
    @Max(value = 90, message = "Los dias de entrega deben ser maximo 90")
    private Integer diasEntrega;
}
