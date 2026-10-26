package Backendcom.example.Backend.service;

import Backendcom.example.Backend.entity.Producto;
import Backendcom.example.Backend.entity.Usuario;
import Backendcom.example.Backend.exception.ResourceNotFoundException;
import Backendcom.example.Backend.repository.ProductoRepository;
import Backendcom.example.Backend.repository.UsuarioRepository;
import Backendcom.example.Backend.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReporteRepository reporteRepository;

    public List<Producto> obtenerProductosPorUsuario(Long idUsuario) {
        return productoRepository.findByUsuarioIdUsuario(idUsuario);
    }

    public Producto obtenerProductoPorId(Long idProducto) {
        return productoRepository.findById(idProducto)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + idProducto));
    }

    public Producto crearProducto(Producto producto, Long idUsuario) {
        // Obtener el usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));
        
        // Asignar el usuario al producto
        producto.setUsuario(usuario);
        if (producto.getEstado() == null || producto.getEstado().isBlank()) {
            producto.setEstado("activo");
        } else {
            producto.setEstado(producto.getEstado().toLowerCase());
        }
        
        // Calcular margen si no está especificado
        if (producto.getMargenGananciaPct() == null && producto.getPrecioCosto() != null && producto.getPrecioCosto().compareTo(BigDecimal.ZERO) > 0 && producto.getPrecioVenta() != null) {
            BigDecimal diferencia = producto.getPrecioVenta().subtract(producto.getPrecioCosto());
            BigDecimal margen = diferencia.multiply(BigDecimal.valueOf(100))
                .divide(producto.getPrecioCosto(), 2, java.math.RoundingMode.HALF_UP);
            producto.setMargenGananciaPct(margen);
        }
        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public Page<Producto> obtenerProductosPaginados(Long idUsuario, int page, int size, String q) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombreProducto"));
        if (q == null || q.trim().isEmpty()) {
            return productoRepository.findByUsuarioIdUsuario(idUsuario, pageable);
        }
        return productoRepository.buscarPaginado(idUsuario, q.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarConEntityManager(String termino, Long idUsuario) {
        if (termino == null || termino.trim().isEmpty()) {
            return productoRepository.findByUsuarioIdUsuario(idUsuario);
        }
        return reporteRepository.buscarProductosAvanzado(idUsuario, termino);
    }

    public Producto actualizarProducto(Long idProducto, Producto productoActualizado) {
        Producto producto = obtenerProductoPorId(idProducto);

        if (productoActualizado.getNombreProducto() != null) {
            producto.setNombreProducto(productoActualizado.getNombreProducto());
        }
        if (productoActualizado.getPrecioCosto() != null) {
            producto.setPrecioCosto(productoActualizado.getPrecioCosto());
        }
        if (productoActualizado.getPrecioVenta() != null) {
            producto.setPrecioVenta(productoActualizado.getPrecioVenta());
        }
        if (productoActualizado.getDescripcion() != null) {
            producto.setDescripcion(productoActualizado.getDescripcion());
        }
        if (productoActualizado.getCategoria() != null) {
            producto.setCategoria(productoActualizado.getCategoria());
        }
        if (productoActualizado.getStockTotal() != null) {
            producto.setStockTotal(productoActualizado.getStockTotal());
        }
        if (productoActualizado.getEstado() != null) {
            producto.setEstado(productoActualizado.getEstado().toLowerCase());
        }

        // Recalcular margen
        if (producto.getPrecioCosto() != null && producto.getPrecioCosto().compareTo(BigDecimal.ZERO) > 0 && producto.getPrecioVenta() != null) {
            BigDecimal diferencia = producto.getPrecioVenta().subtract(producto.getPrecioCosto());
            BigDecimal margen = diferencia.multiply(BigDecimal.valueOf(100))
                .divide(producto.getPrecioCosto(), 2, java.math.RoundingMode.HALF_UP);
            producto.setMargenGananciaPct(margen);
        }

        return productoRepository.save(producto);
    }

    public void actualizarPrecioCosto(Long idProducto, BigDecimal nuevoCosto) {
        Producto producto = obtenerProductoPorId(idProducto);
        producto.setPrecioCosto(nuevoCosto);

        // Recalcular margen
        if (producto.getPrecioVenta() != null && nuevoCosto != null && nuevoCosto.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diferencia = producto.getPrecioVenta().subtract(nuevoCosto);
            BigDecimal margen = diferencia.multiply(BigDecimal.valueOf(100))
                .divide(nuevoCosto, 2, java.math.RoundingMode.HALF_UP);
            producto.setMargenGananciaPct(margen);
        }

        productoRepository.save(producto);
    }

    public void actualizarStock(Long idProducto, Integer nuevoStock) {
        Producto producto = obtenerProductoPorId(idProducto);
        producto.setStockTotal(nuevoStock);
        productoRepository.save(producto);
    }

    public void eliminarProducto(Long idProducto) {
        if (!productoRepository.existsById(idProducto)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + idProducto);
        }
        productoRepository.deleteById(idProducto);
    }

    // ====== MÉTODOS DE BÚSQUEDA CON JPQL Y @Transactional ======
    
    /**
     * Busca productos por nombre (case-insensitive)
     * @Transactional: Garantiza consistencia de datos, rollback automático si falla
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre, Long idUsuario) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El término de búsqueda no puede estar vacío");
            }
            return productoRepository.buscarPorNombre(nombre, idUsuario);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error en búsqueda: " + e.getMessage());
        }
    }

    /**
     * Búsqueda general por nombre o SKU
     * @Transactional: readOnly=true porque solo consulta, no modifica datos
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarGeneral(String busqueda, Long idUsuario) {
        try {
            if (busqueda == null || busqueda.trim().isEmpty()) {
                throw new IllegalArgumentException("El término de búsqueda no puede estar vacío");
            }
            return productoRepository.buscarGeneral(busqueda, idUsuario);
        } catch (Exception e) {
            throw new RuntimeException("Error en búsqueda general: " + e.getMessage());
        }
    }

    /**
     * Busca productos por categoría
     * @Transactional: readOnly=true porque solo consulta
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoria(String categoria, Long idUsuario) {
        try {
            if (categoria == null || categoria.trim().isEmpty()) {
                throw new IllegalArgumentException("La categoría no puede estar vacía");
            }
            return productoRepository.buscarPorCategoria(categoria, idUsuario);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar por categoría: " + e.getMessage());
        }
    }

    /**
     * Obtiene productos con stock por debajo del mínimo
     * @Transactional: readOnly=true para consulta segura
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosConStockBajo(Long idUsuario) {
        try {
            return productoRepository.obtenerProductosConStockBajo(idUsuario);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener productos con stock bajo: " + e.getMessage());
        }
    }
}
