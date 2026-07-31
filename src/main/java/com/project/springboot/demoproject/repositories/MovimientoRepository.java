package com.project.springboot.demoproject.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.springboot.demoproject.entities.Movimiento;
import com.project.springboot.demoproject.enums.TipoMovimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /**
     * Trae usuario, bodegas y detalles en UNA sola consulta (JOIN FETCH),
     * evitando el problema N+1 al listar movimientos. Usar esta version
     * (o una variante con WHERE) en cualquier endpoint que liste varios
     * movimientos a la vez.
     */
    @Query("""
            SELECT DISTINCT m FROM Movimiento m
            LEFT JOIN FETCH m.usuario
            LEFT JOIN FETCH m.bodegaOrigen
            LEFT JOIN FETCH m.bodegaDestino
            LEFT JOIN FETCH m.detalles d
            LEFT JOIN FETCH d.producto
            ORDER BY m.fecha DESC
            """)
    List<Movimiento> findAllConDetalles();

    List<Movimiento> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Movimiento> findByTipo(TipoMovimiento tipo);

    List<Movimiento> findByUsuarioId(Long usuarioId);

    List<Movimiento> findByBodegaOrigenId(Long bodegaId);

    List<Movimiento> findByBodegaDestinoId(Long bodegaId);

    @Query("SELECT m FROM Movimiento m WHERE m.bodegaOrigen.id = :bodegaId OR m.bodegaDestino.id = :bodegaId")
    List<Movimiento> findByBodegaInvolucrada(@Param("bodegaId") Long bodegaId);
}
