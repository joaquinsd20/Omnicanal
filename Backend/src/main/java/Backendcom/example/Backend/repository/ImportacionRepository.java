package Backendcom.example.Backend.repository;

import Backendcom.example.Backend.entity.Importacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImportacionRepository extends JpaRepository<Importacion, Long> {
    Optional<Importacion> findByNumeroFactura(String numeroFactura);
    List<Importacion> findByUsuarioIdUsuario(Long idUsuario);
    List<Importacion> findByEstadoImportacion(String estadoImportacion);
}
