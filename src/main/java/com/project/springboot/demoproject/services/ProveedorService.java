package com.project.springboot.demoproject.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Proveedor buscarPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() ->
                        ResourceNotFoundException.of("Proveedor", id));
    }
}
