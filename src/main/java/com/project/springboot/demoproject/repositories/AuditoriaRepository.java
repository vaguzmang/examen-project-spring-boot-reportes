package com.project.springboot.demoproject.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.springboot.demoproject.entities.Auditoria;
import com.project.springboot.demoproject.enums.TipoOperacionAuditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findByUsuarioId(Long usuarioId);

    List<Auditoria> findByTipoOperacion(TipoOperacionAuditoria tipoOperacion);

    List<Auditoria> findByUsuarioIdAndTipoOperacion(Long usuarioId, TipoOperacionAuditoria tipoOperacion);

    List<Auditoria> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Auditoria> findByEntidadAfectada(String entidadAfectada);

    List<Auditoria> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);
}
