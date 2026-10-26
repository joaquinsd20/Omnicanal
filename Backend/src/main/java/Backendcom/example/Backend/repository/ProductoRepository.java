package Backendcom.example.Backend.repository;

import Backendcom.example.Backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySku(String sku);
    List<Producto> findByUsuarioIdUsuario(Long idUsuario);
    Page<Producto> findByUsuarioIdUsuario(Long idUsuario, Pageable pageable);
    List<Producto> findByUsuarioIdUsuarioAndCategoria(Long idUsuario, String categoria);
    List<Producto> findByUsuarioIdUsuarioAndEstado(Long idUsuario, String estado);
    
    // ====== BÚSQUEDA CON JPQL ======
    /**
     * Busca productos por nombre usando JPQL (LIKE con LOWER para case-insensitive)
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :busqueda, '%')) AND p.usuario.idUsuario = :idUsuario")
    List<Producto> buscarPorNombre(@Param("busqueda") String busqueda, @Param("idUsuario") Long idUsuario);
    
    /**
     * Busca productos por SKU o nombre (búsqueda general)
     */
    @Query("SELECT p FROM Producto p WHERE (LOWER(p.sku) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :busqueda, '%'))) AND p.usuario.idUsuario = :idUsuario")
    List<Producto> buscarGeneral(@Param("busqueda") String busqueda, @Param("idUsuario") Long idUsuario);
    
    /**
     * Busca productos por categoría
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.categoria) LIKE LOWER(CONCAT('%', :categoria, '%')) AND p.usuario.idUsuario = :idUsuario")
    List<Producto> buscarPorCategoria(@Param("categoria") String categoria, @Param("idUsuario") Long idUsuario);
    
    /**
     * Busca productos con stock bajo (por debajo del stock mínimo)
     */
    @Query("SELECT p FROM Producto p WHERE p.stockTotal < p.stockMinimo AND p.usuario.idUsuario = :idUsuario AND UPPER(COALESCE(p.estado, 'ACTIVO')) = 'ACTIVO'")
    List<Producto> obtenerProductosConStockBajo(@Param("idUsuario") Long idUsuario);

    /**
     * Paginación con búsqueda por nombre, SKU o categoría.
     */
    @Query("SELECT p FROM Producto p WHERE p.usuario.idUsuario = :idUsuario AND (:q IS NULL OR :q = '' OR LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(COALESCE(p.categoria, '')) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Producto> buscarPaginado(@Param("idUsuario") Long idUsuario, @Param("q") String q, Pageable pageable);
}
