package Backendcom.example.Backend.controller;

import Backendcom.example.Backend.dto.CrearImportacionRequest;
import Backendcom.example.Backend.dto.ImportacionResponse;
import Backendcom.example.Backend.service.ImportacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/importaciones")
@RequiredArgsConstructor
public class ImportacionController {

    private final ImportacionService importacionService;

    /**
     * Crear una nueva importación con prorrateo automático de costos
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<ImportacionResponse> crearImportacion(
            @Valid @RequestBody CrearImportacionRequest request,
            @RequestHeader("X-User-Id") Long idUsuario,
            @RequestParam(defaultValue = "false") boolean simularError) {
        
        ImportacionResponse response = importacionService.crearImportacionConProrrateo(idUsuario, request, simularError);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener detalles de una importación específica con su prorrateo
     */
    @GetMapping("/{idImportacion}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<ImportacionResponse> obtenerImportacion(
            @PathVariable Long idImportacion) {
        
        ImportacionResponse response = importacionService.obtenerImportacion(idImportacion);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener todas las importaciones del usuario autenticado
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<ImportacionResponse>> obtenerImportacionesDelUsuario(
            @RequestHeader("X-User-Id") Long idUsuario) {
        
        List<ImportacionResponse> importaciones = importacionService.obtenerImportacionesDelUsuario(idUsuario);
        return ResponseEntity.ok(importaciones);
    }
}
