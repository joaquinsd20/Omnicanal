package Backendcom.example.Backend.repository;

import Backendcom.example.Backend.entity.ProrrateoImportacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProrrateoImportacionRepository extends JpaRepository<ProrrateoImportacion, Long> {
    List<ProrrateoImportacion> findByImportacionIdImportacion(Long idImportacion);
    List<ProrrateoImportacion> findByProductoIdProducto(Long idProducto);
}
