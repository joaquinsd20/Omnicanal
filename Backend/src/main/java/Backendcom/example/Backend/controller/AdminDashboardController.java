package Backendcom.example.Backend.controller;

import Backendcom.example.Backend.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Dashboard exclusivo para el ADMINISTRADOR_GLOBAL.
 *
 * La URL final, por el context-path, es:
 * GET http://localhost:8080/api/admin/dashboard/resumen
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumenGlobal(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sesión no válida. Inicia sesión nuevamente."));
        }

        String email = authentication.getName();

        if (!adminDashboardService.esAdministradorGlobal(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Este panel es exclusivo para ADMINISTRADOR_GLOBAL."));
        }

        return ResponseEntity.ok(adminDashboardService.obtenerResumenGlobal());
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "usuario", authentication == null ? "sin autenticación" : authentication.getName(),
                "endpoint", "/api/admin/dashboard/resumen"
        ));
    }
}
