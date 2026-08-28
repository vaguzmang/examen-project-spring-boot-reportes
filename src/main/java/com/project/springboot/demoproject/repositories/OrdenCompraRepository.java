package com.project.springboot.demoproject.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.enums.EstadoOrdenCompra;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    List<OrdenCompra> findByEstadoOrderByFechaCreacionDesc(
            EstadoOrdenCompra estado);

    List<OrdenCompra> findAllByOrderByFechaCreacionDesc();

    long countByEstado(EstadoOrdenCompra estado);
}
