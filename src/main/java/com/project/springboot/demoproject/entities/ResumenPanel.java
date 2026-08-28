package com.project.springboot.demoproject.entities;

import java.time.LocalDate;

import com.project.springboot.demoproject.audit.Auditable;
import com.project.springboot.demoproject.audit.AuditoriaEntityListener;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "resumen_panel",
    uniqueConstraints = @UniqueConstraint(columnNames = "fecha")
)
@EntityListeners(AuditoriaEntityListener.class)
public class ResumenPanel implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(
        name = "contenido_json",
        nullable = false,
        columnDefinition = "TEXT"
    )
    private String contenidoJson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Override
    public String getNombreEntidad() {
        return "resumen_panel";
    }

    @Override
    public Long getEntidadId() {
        return id;
    }
}
