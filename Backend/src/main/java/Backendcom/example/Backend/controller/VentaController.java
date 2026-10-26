package Backendcom.example.Backend.controller;

import Backendcom.example.Backend.entity.DetalleVenta;
import Backendcom.example.Backend.entity.Venta;
import Backendcom.example.Backend.entity.Producto;
import Backendcom.example.Backend.entity.Usuario;
import Backendcom.example.Backend.entity.Canal;
import Backendcom.example.Backend.dto.VentaDTO;
import Backendcom.example.Backend.dto.DetalleVentaDTO;
import Backendcom.example.Backend.repository.DetalleVentaRepository;
import Backendcom.example.Backend.repository.VentaRepository;
import Backendcom.example.Backend.repository.ProductoRepository;
import Backendcom.example.Backend.repository.UsuarioRepository;
import Backendcom.example.Backend.repository.CanalRepository;
import Backendcom.example.Backend.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;
import java.util.Random;

@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CanalRepository canalRepository;
    private final MovimientoInventarioService movimientoInventarioService;

    @GetMapping
    public ResponseEntity<List<Venta>> getAllVentas() {
        return ResponseEntity.ok(ventaRepository.findAll());
    }

    /** Paginación con Spring Boot para ventas. */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Page<Venta>> getVentasPaginadas(
            @RequestHeader("X-User-Id") Long idUsuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaVenta"));
        return ResponseEntity.ok(ventaRepository.findByUsuarioIdUsuario(idUsuario, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> getVentaById(@PathVariable Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        return ResponseEntity.ok(venta);
    }

    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<Venta>> getVentasByUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(ventaRepository.findByUsuarioIdUsuario(idUsuario));
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Venta> createVenta(@Valid @RequestBody VentaDTO ventaDTO,
                                            @RequestHeader("X-User-Id") Long idUsuario,
                                            @RequestParam(defaultValue = "false") boolean simularError) {
        // Crear entidad Venta desde DTO
        Venta venta = new Venta();
        venta.setNombreCliente(ventaDTO.getNombreCliente());
        venta.setEmailCliente(ventaDTO.getEmailCliente());
        venta.setTelefonoCliente(ventaDTO.getTelefonoCliente());
        venta.setRucCliente(ventaDTO.getRucCliente()); // RUC para comprobantes SUNAT
        venta.setProvinciaDestino(ventaDTO.getProvinciaDestino());
        venta.setDistritoDestino(ventaDTO.getDistritoDestino());
        venta.setDireccionEntrega(ventaDTO.getDireccionEntrega());
        venta.setSubtotal(ventaDTO.getSubtotal());
        venta.setCostoEnvio(ventaDTO.getCostoEnvio());
        venta.setDescuento(ventaDTO.getDescuento());
        venta.setTotal(ventaDTO.getTotal());
        venta.setAgenciaEnvio(ventaDTO.getAgenciaEnvio());
        venta.setEstadoPago(ventaDTO.getEstadoPago() != null ? ventaDTO.getEstadoPago() : "pendiente");
        venta.setEstadoVenta("pendiente");
        
        // Generar numeroVenta ÚNICO: V-TIMESTAMP-NANO-UUID (siempre generar en servidor)
        long timestamp = System.currentTimeMillis();
        long nanoSecond = System.nanoTime() % 1000;
        String uuid = java.util.UUID.randomUUID().toString().substring(0, 8);
        venta.setNumeroVenta("V-" + timestamp + "-" + nanoSecond + "-" + uuid);
        
        // Generar numeroTracking automáticamente: AGENCIA-UUID
        String agencia = venta.getAgenciaEnvio() != null ? venta.getAgenciaEnvio() : "TRANSPORTE";
        String numeroTracking = agencia.toUpperCase().replace(" ", "-") + "-" + java.util.UUID.randomUUID().toString().substring(0, 12);
        venta.setNumeroTracking(numeroTracking);
        
        // Cargar Usuario desde BD usando el ID del header (X-User-Id)
        Usuario usuarioActual = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        venta.setUsuario(usuarioActual);
        
        // Cargar Canal desde el DTO, o usar canal por defecto
        Canal canal = null;
        if (ventaDTO.getIdCanal() != null && ventaDTO.getIdCanal() > 0) {
            canal = canalRepository.findById(ventaDTO.getIdCanal())
                    .orElseThrow(() -> new IllegalArgumentException("Canal no encontrado: " + ventaDTO.getIdCanal()));
        } else {
            // Usar primer canal activo del usuario
            canal = canalRepository.findByUsuarioIdUsuarioAndEstado(idUsuario, "activo")
                    .stream()
                    .findFirst()
                    .orElseGet(() -> canalRepository.findByUsuarioIdUsuarioAndEstado(idUsuario, "ACTIVO")
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No hay canales disponibles para este usuario")));
        }
        venta.setCanal(canal);
        
        Venta saved = ventaRepository.save(venta);

        // Si hay detalles en el DTO, crearlos en la BD
        if (ventaDTO.getDetalles() != null && !ventaDTO.getDetalles().isEmpty()) {
            for (DetalleVentaDTO detalleDTO : ventaDTO.getDetalles()) {
                // Cargar el Producto desde BD usando idProducto del DTO
                Producto producto = productoRepository.findById(detalleDTO.getIdProducto())
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + detalleDTO.getIdProducto()));
                
                // Crear DetalleVenta y asociar a la venta
                DetalleVenta detalle = new DetalleVenta();
                detalle.setVenta(saved);
                detalle.setProducto(producto);
                detalle.setCantidad(detalleDTO.getCantidad());
                detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
                detalle.setSubtotal(detalleDTO.getSubtotal());
                detalle.setDescuentoUnitario(detalleDTO.getDescuentoUnitario());
                
                detalleVentaRepository.save(detalle);
            }
        }

        // Deducir stock y registrar movimientos de inventario (SALIDA) para cada producto vendido
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaIdVenta(saved.getIdVenta());
        for (DetalleVenta detalle : detalles) {
            // Cargar el producto y deducir stock
            Producto producto = productoRepository.findById(detalle.getProducto().getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            
            // Validar que hay stock suficiente
            if (producto.getStockTotal() < detalle.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombreProducto());
            }
            
            // El movimiento de inventario descuenta el stock dentro de una transacción.
            // Antes se descontaba aquí y luego otra vez en registrarMovimiento(), causando doble salida.
            // Registrar movimiento de inventario
            movimientoInventarioService.registrarMovimiento(
                idUsuario,
                detalle.getProducto().getIdProducto(),
                "salida",
                detalle.getCantidad(),
                "VENTA-" + saved.getNumeroVenta(),
                "Venta a " + saved.getNombreCliente()
            );
        }

        if (simularError) {
            throw new RuntimeException("Rollback simulado: la venta fue revertida para demostrar @Transactional.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Venta> updateVenta(@PathVariable Long id,
                                             @Valid @RequestBody Venta venta,
                                             @RequestHeader("X-User-Id") Long idUsuario) {
        Venta existente = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        venta.setIdVenta(id);
        venta.setUsuario(existente.getUsuario());
        
        Venta updated = ventaRepository.save(venta);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Void> deleteVenta(@PathVariable Long id) {
        if (!ventaRepository.existsById(id)) {
            throw new IllegalArgumentException("Venta no encontrada");
        }
        ventaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener ventas agrupadas por canal del usuario
     * GET /api/ventas/resumen/por-canal
     */
    @GetMapping("/resumen/por-canal")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<?> ventasPorCanal(@RequestHeader("X-User-Id") Long idUsuario) {
        try {
            List<Venta> ventasDelUsuario = ventaRepository.findByUsuarioIdUsuario(idUsuario);
            
            java.util.Map<Long, java.util.Map<String, Object>> resumen = new java.util.HashMap<>();
            
            for (Venta venta : ventasDelUsuario) {
                if (venta.getCanal() != null) {
                    Long canalId = venta.getCanal().getIdCanal();
                    
                    if (!resumen.containsKey(canalId)) {
                        java.util.Map<String, Object> datos = new java.util.HashMap<>();
                        datos.put("idCanal", venta.getCanal().getIdCanal());
                        datos.put("nombreCanal", venta.getCanal().getNombreCanal());
                        datos.put("tipoCanal", venta.getCanal().getTipoCanal());
                        datos.put("cantidadVentas", 0);
                        datos.put("montoTotal", java.math.BigDecimal.ZERO);
                        resumen.put(canalId, datos);
                    }
                    
                    java.util.Map<String, Object> datos = resumen.get(canalId);
                    Integer cantidad = (Integer) datos.get("cantidadVentas");
                    java.math.BigDecimal monto = (java.math.BigDecimal) datos.get("montoTotal");
                    
                    datos.put("cantidadVentas", cantidad + 1);
                    java.math.BigDecimal totalVenta = venta.getTotal() != null ? venta.getTotal() : java.math.BigDecimal.ZERO;
                    datos.put("montoTotal", monto.add(totalVenta));
                }
            }
            
            return ResponseEntity.ok(new java.util.ArrayList<>(resumen.values()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
