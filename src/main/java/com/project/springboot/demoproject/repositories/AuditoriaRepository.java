package com.project.springboot.demoproject.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.springboot.demoproject.entities.Auditoria;
import com.project.springboot.demoproject.enums.TipoOperacionAuditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findByUsuarioId(Long usuarioId);

    List<Auditoria> findByTipoOperacion(TipoOperacionAuditoria tipoOperacion);

    List<Auditoria> findByUsuarioIdAndTipoOperacion(
            Long usuarioId,
            TipoOperacionAuditoria tipoOperacion);

    List<Auditoria> findByFechaHoraBetween(
            LocalDateTime inicio,
            LocalDateTime fin);

    List<Auditoria> findByEntidadAfectada(String entidadAfectada);

    List<Auditoria> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);

    /**
     * Reporte de auditoría con filtros opcionales.
     */
    @Query("""
            SELECT a
            FROM Auditoria a
            WHERE
                (:productoId IS NULL OR a.productoId = :productoId)
            AND (:campoModificado IS NULL OR LOWER(a.campoModificado) LIKE LOWER(CONCAT('%', :campoModificado, '%')))
            AND (:fechaInicio IS NULL OR a.fechaHora >= :fechaInicio)
            AND (:fechaFin IS NULL OR a.fechaHora <= :fechaFin)
            ORDER BY a.fechaHora DESC
            """)
    List<Auditoria> obtenerAuditoriaReporte(
            @Param("productoId") Long productoId,
            @Param("campoModificado") String campoModificado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

}