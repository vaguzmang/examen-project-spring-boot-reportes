package com.project.springboot.demoproject.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.BodegaRequest;
import com.project.springboot.demoproject.dto.BodegaResponse;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.exception.DuplicateResourceException;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoRepository;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BodegaService {

    private final BodegaRepository bodegaRepository;
    private final MovimientoRepository movimientoRepository;
    private final OrdenCompraRepository ordenCompraRepository;

    public List<BodegaResponse> listarTodas() {
        return bodegaRepository.findAll().stream().map(BodegaResponse::desde).toList();
    }

    public BodegaResponse obtenerPorId(Long id) {
        return BodegaResponse.desde(buscarPorId(id));
    }

    public Bodega buscarPorId(Long id) {
        return bodegaRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Bodega", id));
    }

    @Transactional
    public BodegaResponse crear(BodegaRequest request) {
        if (bodegaRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException("Ya existe una bodega con el nombre '" + request.getNombre() + "'");
        }
        Bodega bodega = new Bodega();
        mapear(request, bodega);
        return BodegaResponse.desde(bodegaRepository.save(bodega));
    }

    @Transactional
    public BodegaResponse actualizar(Long id, BodegaRequest request) {
        Bodega bodega = buscarPorId(id);
        mapear(request, bodega);
        return BodegaResponse.desde(bodegaRepository.save(bodega));
    }

    @Transactional
    public void eliminar(Long id) {

        Bodega bodega = buscarPorId(id);

        if (movimientoRepository
                .existsByBodegaOrigenIdOrBodegaDestinoId(id, id)) {

            throw new DuplicateResourceException(
                    "La bodega posee historial de movimientos y no puede eliminarse");
        }

        if (ordenCompraRepository.existsByBodegaDestinoId(id)) {

            throw new DuplicateResourceException(
                    "La bodega posee historial de órdenes de compra y no puede eliminarse");
        }

        bodegaRepository.delete(bodega);
    }

    public List<BodegaResponse> buscarPorUbicacion(String ubicacion) {
        return bodegaRepository.findByUbicacionContainingIgnoreCase(ubicacion).stream()
                .map(BodegaResponse::desde).toList();
    }

    private void mapear(BodegaRequest request, Bodega bodega) {
        bodega.setNombre(request.getNombre());
        bodega.setUbicacion(request.getUbicacion());
        bodega.setCapacidad(request.getCapacidad());
        bodega.setEncargado(request.getEncargado());
    }
}
