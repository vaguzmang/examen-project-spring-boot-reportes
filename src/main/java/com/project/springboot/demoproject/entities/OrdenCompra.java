package com.project.springboot.demoproject.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.project.springboot.demoproject.audit.Auditable;
import com.project.springboot.demoproject.audit.AuditoriaEntityListener;
import com.project.springboot.demoproject.enums.EstadoOrdenCompra;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orden_compra")
@EntityListeners(AuditoriaEntityListener.class)
public class OrdenCompra implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bodega_destino_id", nullable = false)
    private Bodega bodegaDestino;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrdenCompra estado = EstadoOrdenCompra.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @Lob
    @Column(name = "pdf")
    private byte[] pdf;

    @Column(name = "fecha_generacion_pdf")
    private LocalDateTime fechaGeneracionPdf;

    @Override
    public String getNombreEntidad() {
        return "orden_compra";
    }

    @Override
    public Long getEntidadId() {
        return id;
    }
}
