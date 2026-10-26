package Backendcom.example.Backend.repository;

import Backendcom.example.Backend.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByUsuarioIdUsuario(Long idUsuario);
    List<MovimientoInventario> findByProductoIdProducto(Long idProducto);
    List<MovimientoInventario> findByTipoMovimiento(String tipoMovimiento);
}
