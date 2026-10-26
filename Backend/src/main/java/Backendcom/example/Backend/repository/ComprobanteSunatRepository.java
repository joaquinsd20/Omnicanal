package Backendcom.example.Backend.repository;

import Backendcom.example.Backend.entity.ComprobanteSunat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobanteSunatRepository extends JpaRepository<ComprobanteSunat, Long> {
    
    /**
     * Obtiene el comprobante de una venta
     */
    Optional<ComprobanteSunat> findByVentaIdVenta(Long idVenta);
    
    /**
     * Obtiene comprobantes por estado
     */
    List<ComprobanteSunat> findByEstado(String estado);
    
    /**
     * Obtiene comprobantes pendientes de envío
     */
    @Query("SELECT c FROM ComprobanteSunat c WHERE c.estado = 'PENDIENTE'")
    List<ComprobanteSunat> obtenerComprobantesPendientes();
    
    /**
     * Obtiene comprobantes rechazados por SUNAT
     */
    @Query("SELECT c FROM ComprobanteSunat c WHERE c.estado = 'RECHAZADO' ORDER BY c.fechaCreacion DESC")
    List<ComprobanteSunat> obtenerComprobantesRechazados();
    
    /**
     * Cuenta comprobantes aceptados
     */
    long countByEstado(String estado);
}
