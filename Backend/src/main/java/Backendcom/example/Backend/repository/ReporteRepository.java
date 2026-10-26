package Backendcom.example.Backend.repository;

import Backendcom.example.Backend.entity.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio personalizado con EntityManager.
 * Se incluye para cubrir el tema del sílabo: acceso a datos con
 * EntityManager + @Repository, además de JPQL con parámetros nombrados.
 */
@Repository
public class ReporteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Búsqueda avanzada con JPQL + named parameters.
     * Permite demostrar una consulta manual distinta a JpaRepository.
     */
    public List<Producto> buscarProductosAvanzado(Long idUsuario, String termino) {
        String jpql = """
            SELECT p
            FROM Producto p
            WHERE p.usuario.idUsuario = :idUsuario
              AND (
                    LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :termino, '%'))
                 OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :termino, '%'))
                 OR LOWER(COALESCE(p.categoria, '')) LIKE LOWER(CONCAT('%', :termino, '%'))
              )
            ORDER BY p.nombreProducto ASC
            """;

        return entityManager.createQuery(jpql, Producto.class)
                .setParameter("idUsuario", idUsuario)
                .setParameter("termino", termino == null ? "" : termino.trim())
                .getResultList();
    }
}
