package Backendcom.example.Backend.repository;

import Backendcom.example.Backend.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    Optional<Venta> findByNumeroVenta(String numeroVenta);
    List<Venta> findByUsuarioIdUsuario(Long idUsuario);
    Page<Venta> findByUsuarioIdUsuario(Long idUsuario, Pageable pageable);
    List<Venta> findByUsuarioIdUsuarioAndFechaVentaBetween(Long idUsuario, LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByEstadoVenta(String estadoVenta);
}
