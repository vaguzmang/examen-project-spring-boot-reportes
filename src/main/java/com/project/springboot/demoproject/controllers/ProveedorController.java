package com.project.springboot.demoproject.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.ProveedorResponse;
import com.project.springboot.demoproject.services.ProveedorService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
@Tag(name = "Proveedores")
@SecurityRequirement(name = "bearerAuth")
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
    public List<ProveedorResponse> listar() {
        return proveedorService.listar();
    }
}
