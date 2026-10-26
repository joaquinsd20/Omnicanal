package Backendcom.example.Backend.controller;

import Backendcom.example.Backend.dto.ConfiguracionSunatDTO;
import Backendcom.example.Backend.dto.SunatResumenDTO;
import Backendcom.example.Backend.dto.SunatValidationRequestDTO;
import Backendcom.example.Backend.entity.ComprobanteSunat;
import Backendcom.example.Backend.entity.ConfiguracionSunat;
import Backendcom.example.Backend.service.SunatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para integración con SUNAT
 * Endpoints para gestionar configuración fiscal y comprobantes electrónicos
 */
@RestController
@RequestMapping("/sunat")
@RequiredArgsConstructor
@Slf4j
public class SunatController {
    
    private final SunatService sunatService;
    
    // ===== CONFIGURACIÓN SUNAT =====
    
    /**
     * Registra o actualiza datos fiscales SUNAT del usuario
     * POST /api/sunat/configuracion
     */
    @PostMapping("/configuracion")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<ConfiguracionSunat> guardarConfiguracion(
            @RequestHeader("X-User-Id") Long idUsuario,
            @RequestParam("ruc") String ruc,
            @RequestParam("razonSocial") String razonSocial,
            @RequestParam(value = "nombreComercial", required = false) String nombreComercial,
            @RequestParam(value = "domicilioFiscal", required = false) String domicilioFiscal,
            @RequestParam(value = "numeroSerieFactura", defaultValue = "F001") String numeroSerieFactura,
            @RequestParam(value = "numeroSerieBoleta", defaultValue = "B001") String numeroSerieBoleta,
            @RequestParam(value = "passwordCertificado", required = false) String passwordCertificado,
            @RequestParam(value = "pruebasMode", defaultValue = "true") Boolean pruebasMode,
            @RequestParam(value = "certificado", required = false) MultipartFile certificado) {
        
        log.info("Guardando configuración SUNAT para usuario: {}", idUsuario);
        
        try {
            ConfiguracionSunatDTO dto = ConfiguracionSunatDTO.builder()
                .ruc(ruc)
                .razonSocial(razonSocial)
                .nombreComercial(nombreComercial)
                .domicilioFiscal(domicilioFiscal)
                .numeroSerieFactura(numeroSerieFactura)
                .numeroSerieBoleta(numeroSerieBoleta)
                .passwordCertificado(passwordCertificado)
                .pruebasMode(pruebasMode)
                .activo(true)
                .build();
            
            ConfiguracionSunat config = sunatService.guardarConfiguracion(idUsuario, dto, certificado);
            return ResponseEntity.ok(config);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtiene la configuración SUNAT actual
     * GET /api/sunat/configuracion
     */
    @GetMapping("/configuracion")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<ConfiguracionSunat> obtenerConfiguracion(
            @RequestHeader("X-User-Id") Long idUsuario) {
        
        ConfiguracionSunat config = sunatService.obtenerConfiguracion(idUsuario);
        // No retornar certificado en la respuesta
        config.setCertificadoDigital(null);
        config.setPasswordCertificado(null);
        return ResponseEntity.ok(config);
    }
    
    /**
     * Verifica si el usuario tiene configuración SUNAT
     * GET /api/sunat/configuracion/existe
     */
    @GetMapping("/configuracion/existe")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Boolean> tieneConfiguracion(
            @RequestHeader("X-User-Id") Long idUsuario) {
        
        boolean existe = sunatService.tieneConfiguracionSunat(idUsuario);
        return ResponseEntity.ok(existe);
    }
    
    // ===== GENERACIÓN Y ENVÍO DE COMPROBANTES =====
    
    /**
     * Prepara un comprobante (factura o boleta) para una venta
     * POST /api/sunat/comprobantes/{idVenta}?tipo=01
     */
    @PostMapping("/comprobantes/{idVenta}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<ComprobanteSunat> prepararComprobante(
            @PathVariable Long idVenta,
            @RequestParam(value = "tipo", defaultValue = "03") String tipoComprobante) {
        
        log.info("Preparando comprobante {} para venta: {}", tipoComprobante, idVenta);
        
        try {
            ComprobanteSunat comprobante = sunatService.prepararComprobante(idVenta, tipoComprobante);
            return ResponseEntity.ok(comprobante);
        } catch (Exception e) {
            log.error("Error preparando comprobante: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Firma y envía comprobante a SUNAT
     * POST /api/sunat/comprobantes/{idComprobante}/enviar
     */
    @PostMapping("/comprobantes/{idComprobante}/enviar")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<ComprobanteSunat> enviarComprobante(
            @PathVariable Long idComprobante) {
        
        log.info("Enviando comprobante a SUNAT: {}", idComprobante);
        
        try {
            // Primero firma
            sunatService.firmarComprobante(idComprobante);
            // Luego envía
            sunatService.enviarAsunat(idComprobante);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error enviando comprobante: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtiene comprobantes pendientes de envío
     * GET /api/sunat/comprobantes/pendientes
     */
    @GetMapping("/comprobantes/pendientes")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<ComprobanteSunat>> obtenerComprobantesPendientes() {
        List<ComprobanteSunat> pendientes = sunatService.obtenerComprobantesPendientes();
        return ResponseEntity.ok(pendientes);
    }
    
    /**
     * Obtiene comprobantes rechazados
     * GET /api/sunat/comprobantes/rechazados
     */
    @GetMapping("/comprobantes/rechazados")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<ComprobanteSunat>> obtenerComprobantesRechazados() {
        List<ComprobanteSunat> rechazados = sunatService.obtenerComprobantesRechazados();
        return ResponseEntity.ok(rechazados);
    }
    
    /**
     * Obtiene resumen de estado de comprobantes
     * GET /api/sunat/resumen
     */
    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<SunatResumenDTO> obtenerResumen(
            @RequestHeader("X-User-Id") Long idUsuario) {
        SunatResumenDTO resumen = sunatService.obtenerResumen(idUsuario);
        return ResponseEntity.ok(resumen);
    }
    
    // ===== ENDPOINTS PARA PRUEBAS (TEST) =====
    
    /**
     * Proxy para generar token SUNAT desde el backend
     * Evita problemas CORS del frontend
     * POST /api/sunat/test/token
     */
    @PostMapping("/test/token")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> generarTokenTest(
            @RequestParam String clientId,
            @RequestParam String clientSecret) {
        
        log.info("Solicitando token SUNAT de prueba");
        try {
            Object tokenResponse = sunatService.generarTokenDesdeBackend(clientId, clientSecret);
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("Error generando token: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponse("Error generando token: " + e.getMessage()));
        }
    }
    
    /**
     * Proxy para validar comprobante SUNAT desde el backend
     * Evita problemas CORS del frontend
     * POST /api/sunat/test/validar
     */
    @PostMapping("/test/validar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> validarComprobanteTest(
            @RequestParam String token,
            @RequestBody SunatValidationRequestDTO validationRequest) {
        
        log.info("Validando comprobante: {}", validationRequest.getNumeroComprobante());
        try {
            Object validationResponse = sunatService.validarComprobanteDesdeBackend(token, validationRequest);
            return ResponseEntity.ok(validationResponse);
        } catch (Exception e) {
            log.error("Error validando comprobante: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponse("Error validando comprobante: " + e.getMessage()));
        }
    }
    
    /**
     * Clase interna para respuestas de error
     */
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    // ===== ENDPOINTS PÚBLICOS DE PRUEBA =====
    
    /**
     * Endpoint simple de prueba (sin autenticación)
     * GET /api/sunat/health
     */
    @GetMapping("/health")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("✅ SUNAT Controller is running");
    }
}
