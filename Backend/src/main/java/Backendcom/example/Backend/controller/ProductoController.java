package Backendcom.example.Backend.controller;

import Backendcom.example.Backend.entity.Producto;
import Backendcom.example.Backend.exception.BusinessException;
import Backendcom.example.Backend.repository.ProductoRepository;
import Backendcom.example.Backend.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoRepository productoRepository;

    @GetMapping
    public ResponseEntity<List<Producto>> getAllProductos(@RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        if (idUsuario != null) {
            return ResponseEntity.ok(productoService.obtenerProductosPorUsuario(idUsuario));
        }
        return ResponseEntity.ok(productoRepository.findAll());
    }

    /**
     * Paginación con Spring Boot.
     * Ejemplo: GET /api/productos/page?page=0&size=5&q=phone
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Page<Producto>> getProductosPaginados(
            @RequestHeader("X-User-Id") Long idUsuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(productoService.obtenerProductosPaginados(idUsuario, page, size, q));
    }

    /**
     * Búsqueda con EntityManager + @Repository personalizado.
     * Evidencia directa del tema del sílabo.
     */
    @GetMapping("/buscar/entity-manager")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<Producto>> buscarConEntityManager(
            @RequestParam(defaultValue = "") String q,
            @RequestHeader("X-User-Id") Long idUsuario) {
        return ResponseEntity.ok(productoService.buscarConEntityManager(q, idUsuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) {
        Producto producto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<Producto>> getProductosByUsuario(@PathVariable Long idUsuario) {
        List<Producto> productos = productoService.obtenerProductosPorUsuario(idUsuario);
        return ResponseEntity.ok(productos);
    }

    // ====== ENDPOINTS DE BÚSQUEDA CON JPQL Y @Transactional ======
    
    /**
     * Búsqueda por nombre de producto
     * GET /api/productos/buscar/nombre?q=Laptop&idUsuario=1
     */
    @GetMapping("/buscar/nombre")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam("q") String nombre,
                                                          @RequestHeader("X-User-Id") Long idUsuario) {
        try {
            List<Producto> productos = productoService.buscarPorNombre(nombre, idUsuario);
            return ResponseEntity.ok(productos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Búsqueda general (nombre o SKU)
     * GET /api/productos/buscar/general?q=ASUS&idUsuario=1
     */
    @GetMapping("/buscar/general")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<Producto>> buscarGeneral(@RequestParam("q") String busqueda,
                                                        @RequestHeader("X-User-Id") Long idUsuario) {
        try {
            List<Producto> productos = productoService.buscarGeneral(busqueda, idUsuario);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Búsqueda por categoría
     * GET /api/productos/buscar/categoria?categoria=Computadoras&idUsuario=1
     */
    @GetMapping("/buscar/categoria")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<Producto>> buscarPorCategoria(@RequestParam("categoria") String categoria,
                                                             @RequestHeader("X-User-Id") Long idUsuario) {
        try {
            List<Producto> productos = productoService.buscarPorCategoria(categoria, idUsuario);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Obtiene productos con stock bajo
     * GET /api/productos/stock-bajo?idUsuario=1
     */
    @GetMapping("/stock-bajo")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<Producto>> obtenerProductosConStockBajo(@RequestHeader("X-User-Id") Long idUsuario) {
        try {
            List<Producto> productos = productoService.obtenerProductosConStockBajo(idUsuario);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Producto> createProducto(@Valid @RequestBody Producto producto,
                                                    @RequestHeader("X-User-Id") Long idUsuario) {
        productoRepository.findBySku(producto.getSku()).ifPresent(p -> {
            throw new BusinessException("Ya existe un producto con el SKU '" + producto.getSku() + "'.");
        });
        validarPrecios(producto);
        Producto saved = productoService.crearProducto(producto, idUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Producto> updateProducto(@PathVariable Long id,
                                                   @Valid @RequestBody Producto producto) {
        Producto existente = productoService.obtenerProductoPorId(id);

        // Si cambió el SKU, validar que no choque con otro producto.
        productoRepository.findBySku(producto.getSku()).ifPresent(p -> {
            if (!p.getIdProducto().equals(id)) {
                throw new BusinessException("El SKU '" + producto.getSku() + "' ya pertenece a otro producto.");
            }
        });
        validarPrecios(producto);

        Producto actualizado = productoService.actualizarProducto(id, producto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    /** Regla de negocio: el precio de venta no debe ser menor al de costo. */
    private void validarPrecios(Producto p) {
        if (p.getPrecioCosto() != null && p.getPrecioVenta() != null
                && p.getPrecioVenta().compareTo(p.getPrecioCosto()) < 0) {
            throw new BusinessException("El precio de venta no puede ser menor que el precio de costo.");
        }
    }
}
