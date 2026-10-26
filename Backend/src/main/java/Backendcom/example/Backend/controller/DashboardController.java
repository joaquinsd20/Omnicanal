package Backendcom.example.Backend.controller;

import Backendcom.example.Backend.dto.DashboardResumenDTO;
import Backendcom.example.Backend.dto.VentasPorCanalDTO;
import Backendcom.example.Backend.dto.ProductoTopDTO;
import Backendcom.example.Backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Resumen financiero del mes actual (ingresos, costos, ganancia neta, margen)
     */
    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<DashboardResumenDTO> obtenerResumenFinanciero(
            @RequestHeader("X-User-Id") Long idUsuario) {
        
        DashboardResumenDTO resumen = dashboardService.obtenerResumenFinanciero(idUsuario);
        return ResponseEntity.ok(resumen);
    }

    /**
     * Ingresos desagregados por canal de venta (TiendaWeb, WhatsApp, TikTok, Física, etc)
     */
    @GetMapping("/ventas-por-canal")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<VentasPorCanalDTO>> obtenerVentasPorCanal(
            @RequestHeader("X-User-Id") Long idUsuario) {
        
        List<VentasPorCanalDTO> ventas = dashboardService.obtenerVentasPorCanal(idUsuario);
        return ResponseEntity.ok(ventas);
    }

    /**
     * Top N productos más rentables/vendibles
     */
    @GetMapping("/productos-top")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<ProductoTopDTO>> obtenerProductosTop(
            @RequestHeader("X-User-Id") Long idUsuario,
            @RequestParam(defaultValue = "5") int limite) {
        
        List<ProductoTopDTO> productos = dashboardService.obtenerProductosTop(idUsuario, limite);
        return ResponseEntity.ok(productos);
    }

    /**
     * Resumen de inventario (stock bajo, sin stock, valor total)
     */
    @GetMapping("/inventario")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> obtenerResumenInventario(
            @RequestHeader("X-User-Id") Long idUsuario) {
        
        Map<String, Object> resumen = dashboardService.obtenerResumenInventario(idUsuario);
        return ResponseEntity.ok(resumen);
    }

    /**
     * Tendencia real de ingresos de los últimos 7 días.
     */
    @GetMapping("/tendencia-7-dias")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<Map<String, Object>>> obtenerTendenciaUltimos7Dias(
            @RequestHeader("X-User-Id") Long idUsuario) {
        return ResponseEntity.ok(dashboardService.obtenerTendenciaUltimos7Dias(idUsuario));
    }

    /**
     * Resumen real del día: ingresos, costos, ganancia y cantidad de ventas.
     */
    @GetMapping("/resumen-diario")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> obtenerResumenDiario(
            @RequestHeader("X-User-Id") Long idUsuario) {
        return ResponseEntity.ok(dashboardService.obtenerResumenDiario(idUsuario));
    }

}
