package com.project.springboot.demoproject.entities;

import com.project.springboot.demoproject.audit.Auditable;
import com.project.springboot.demoproject.audit.AuditoriaEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proveedor")
@EntityListeners(AuditoriaEntityListener.class)
public class Proveedor implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 200)
    private String contacto;

    @Min(1)
    @Max(90)
    @Column(name = "dias_entrega", nullable = false)
    private Integer diasEntrega;

    @Override
    public String getNombreEntidad() {
        return "proveedor";
    }

    @Override
    public Long getEntidadId() {
        return id;
    }
}
