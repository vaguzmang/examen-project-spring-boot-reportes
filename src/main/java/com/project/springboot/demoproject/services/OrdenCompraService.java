package com.project.springboot.demoproject.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.audit.CurrentUserProvider;
import com.project.springboot.demoproject.dto.EstadoOrdenCompraRequest;
import com.project.springboot.demoproject.dto.MovimientoDetalleRequest;
import com.project.springboot.demoproject.dto.MovimientoRequest;
import com.project.springboot.demoproject.dto.OrdenCompraRequest;
import com.project.springboot.demoproject.dto.OrdenCompraResponse;
import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.EstadoOrdenCompra;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final ProductoService productoService;
    private final ProveedorService proveedorService;
    private final BodegaService bodegaService;
    private final CurrentUserProvider currentUserProvider;
    private final MovimientoService movimientoService;

    @Transactional
    public OrdenCompraResponse crearBorrador(OrdenCompraRequest request) {

        if (!OrdenCompraReglas.cantidadValida(request.getCantidad())) {
            throw new BusinessException(
                    "La cantidad debe ser mayor a cero");
        }

        Usuario usuario = currentUserProvider.getUsuarioActual()
                .orElseThrow(() ->
                        new BusinessException(
                                "No existe usuario autenticado"));

        OrdenCompra orden = new OrdenCompra();

        orden.setProducto(
                productoService.buscarPorId(request.getProductoId()));

        orden.setProveedor(
                proveedorService.buscarPorId(request.getProveedorId()));

        orden.setBodegaDestino(
                bodegaService.buscarPorId(request.getBodegaDestinoId()));

        orden.setCantidad(request.getCantidad());

        orden.setPrecioUnitario(request.getPrecioUnitario());

        BigDecimal total = request.getPrecioUnitario()
                .multiply(BigDecimal.valueOf(request.getCantidad()));

        orden.setTotal(total);
        orden.setFechaCreacion(LocalDateTime.now(ZoneId.of("America/Bogota")));
        orden.setEstado(EstadoOrdenCompra.BORRADOR);
        orden.setCreadoPor(usuario);

        return OrdenCompraResponse.desde(
                ordenCompraRepository.save(orden));
    }

    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> listar(
            EstadoOrdenCompra estado) {

        List<OrdenCompra> ordenes =
                estado == null
                ? ordenCompraRepository.findAllByOrderByFechaCreacionDesc()
                : ordenCompraRepository
                    .findByEstadoOrderByFechaCreacionDesc(estado);

        return ordenes.stream()
                .map(OrdenCompraResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenCompraResponse obtener(Long id) {
        return OrdenCompraResponse.desde(buscar(id));
    }

    public OrdenCompra buscar(Long id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() ->
                        ResourceNotFoundException.of(
                                "OrdenCompra", id));
    }

    @Transactional
    public OrdenCompraResponse cambiarEstado(
            Long id,
            EstadoOrdenCompraRequest request) {

        OrdenCompra orden = buscar(id);

        EstadoOrdenCompra actual = orden.getEstado();
        EstadoOrdenCompra nuevo = request.getEstado();

        if (!OrdenCompraReglas.puedeTransicionar(
                actual.name(), nuevo.name())) {

            throw new BusinessException(
                    "Transición de orden inválida: "
                    + actual + " -> " + nuevo);
        }

        /*
         * Todo PDF queda inválido al cambiar estado.
         */
        orden.setPdf(null);
        orden.setFechaGeneracionPdf(null);

        /*
         * APROBADA -> RECIBIDA crea ENTRADA dentro
         * de la MISMA transacción.
         */
        if (OrdenCompraReglas.requiereMovimientoEntrada(
                actual.name(), nuevo.name())) {

            registrarEntradaRecepcion(orden);
        }

        orden.setEstado(nuevo);

        return OrdenCompraResponse.desde(
                ordenCompraRepository.save(orden));
    }

    private void registrarEntradaRecepcion(OrdenCompra orden) {

        MovimientoDetalleRequest detalle =
                new MovimientoDetalleRequest();

        detalle.setProductoId(
                orden.getProducto().getId());

        detalle.setCantidad(
                orden.getCantidad());

        MovimientoRequest movimiento =
                new MovimientoRequest();

        movimiento.setTipo(TipoMovimiento.ENTRADA);

        movimiento.setBodegaDestinoId(
                orden.getBodegaDestino().getId());

        movimiento.setBodegaOrigenId(null);

        movimiento.setDetalles(List.of(detalle));

        movimientoService.registrar(movimiento);
    }
}
