package com.project.springboot.demoproject.entities;

import java.math.BigDecimal;

import com.project.springboot.demoproject.audit.Auditable;
import com.project.springboot.demoproject.audit.AuditoriaEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nota: el esquema SQL enviado no tiene columna "stock" en producto (el stock
 * vive por bodega en inventario_bodega). El campo "stock" que pide el enunciado
 * en el CRUD de productos se expone en el DTO como el stock TOTAL sumado de
 * todas las bodegas (ver ProductoService/ProductoDto).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "producto")
@EntityListeners(AuditoriaEntityListener.class)
public class Producto implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_principal_id")
    private Proveedor proveedorPrincipal;

    @Override
    public String getNombreEntidad() {
        return "producto";
    }

    @Override
    public Long getEntidadId() {
        return id;
    }
}
