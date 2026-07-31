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
     * evitando el problema N+1 al listar movimientos.
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

    @Query("""
            SELECT m
            FROM Movimiento m
            WHERE m.bodegaOrigen.id = :bodegaId
               OR m.bodegaDestino.id = :bodegaId
            """)
    List<Movimiento> findByBodegaInvolucrada(@Param("bodegaId") Long bodegaId);

    /**
     * ===============================
     * CONSULTA DEL EXAMEN
     * ===============================
     * Permite filtrar movimientos por:
     * - Bodega
     * - Producto
     * - Tipo de movimiento
     * - Fecha inicial
     * - Fecha final
     *
     * Todos los parámetros son opcionales.
     */
    @Query("""
            SELECT DISTINCT m
            FROM Movimiento m
            JOIN m.detalles md
            JOIN md.producto p
            LEFT JOIN m.bodegaOrigen bo
            LEFT JOIN m.bodegaDestino bd
            WHERE
                (:bodegaId IS NULL
                    OR bo.id = :bodegaId
                    OR bd.id = :bodegaId)
            AND (:productoId IS NULL
                    OR p.id = :productoId)
            AND (:tipoMovimiento IS NULL
                    OR m.tipo = :tipoMovimiento)
            AND (:fechaInicio IS NULL
                    OR m.fecha >= :fechaInicio)
            AND (:fechaFin IS NULL
                    OR m.fecha <= :fechaFin)
            ORDER BY m.fecha DESC
            """)
    List<Movimiento> obtenerMovimientosReporte(
            @Param("bodegaId") Long bodegaId,
            @Param("productoId") Long productoId,
            @Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

}