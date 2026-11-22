package Backendcom.example.Backend.controller;

import Backendcom.example.Backend.entity.MovimientoInventario;
import Backendcom.example.Backend.service.MovimientoInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kardex")
@RequiredArgsConstructor
public class KardexController {

    private final MovimientoInventarioService movimientoInventarioService;

    /**
     * Obtener todos los movimientos de inventario del usuario
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<MovimientoInventario>> obtenerMovimientosPorUsuario(
            @RequestHeader("X-User-Id") Long idUsuario) {
        
        List<MovimientoInventario> movimientos = movimientoInventarioService.obtenerMovimientosPorUsuario(idUsuario);
        return ResponseEntity.ok(movimientos);
    }

    /**
     * Obtener movimientos de un producto específico
     */
    @GetMapping("/producto/{idProducto}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<MovimientoInventario>> obtenerMovimientosDelProducto(
            @PathVariable Long idProducto,
            @RequestHeader("X-User-Id") Long idUsuario) {
        
        // Solo los movimientos del usuario autenticado
        List<MovimientoInventario> movimientos = movimientoInventarioService.obtenerMovimientosDelProducto(idProducto).stream()
                .filter(m -> m.getUsuario() != null && idUsuario.equals(m.getUsuario().getIdUsuario()))
                .toList();
        return ResponseEntity.ok(movimientos);
    }

    /**
     * Obtener movimientos por tipo (entrada, salida, ajuste, devolución)
     */
    @GetMapping("/tipo/{tipoMovimiento}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<MovimientoInventario>> obtenerMovimientosPorTipo(
            @PathVariable String tipoMovimiento,
            @RequestHeader("X-User-Id") Long idUsuario) {
        
        // Solo los movimientos del usuario autenticado
        List<MovimientoInventario> movimientos = movimientoInventarioService.obtenerMovimientosPorTipo(tipoMovimiento).stream()
                .filter(m -> m.getUsuario() != null && idUsuario.equals(m.getUsuario().getIdUsuario()))
                .toList();
        return ResponseEntity.ok(movimientos);
    }
}
