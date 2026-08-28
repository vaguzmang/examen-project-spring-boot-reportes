package com.project.springboot.demoproject.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.ProveedorRequest;
import com.project.springboot.demoproject.dto.ProveedorResponse;
import com.project.springboot.demoproject.entities.Proveedor;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.ProveedorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Transactional(readOnly = true)
    public List<ProveedorResponse> listar() {
        return proveedorRepository.findAll()
                .stream()
                .map(ProveedorResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProveedorResponse obtener(Long id) {
        return ProveedorResponse.desde(buscarPorId(id));
    }

    public Proveedor buscarPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() ->
                        ResourceNotFoundException.of("Proveedor", id));
    }

    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        Proveedor proveedor = new Proveedor();
        mapear(request, proveedor);
        return ProveedorResponse.desde(proveedorRepository.save(proveedor));
    }

    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = buscarPorId(id);
        mapear(request, proveedor);
        return ProveedorResponse.desde(proveedorRepository.save(proveedor));
    }

    @Transactional
    public void eliminar(Long id) {
        proveedorRepository.delete(buscarPorId(id));
    }

    private void mapear(ProveedorRequest request, Proveedor proveedor) {
        proveedor.setNombre(request.getNombre().trim());

        String contacto = request.getContacto();
        proveedor.setContacto(
                contacto == null || contacto.isBlank()
                        ? null
                        : contacto.trim()
        );

        proveedor.setDiasEntrega(request.getDiasEntrega());
    }
}
