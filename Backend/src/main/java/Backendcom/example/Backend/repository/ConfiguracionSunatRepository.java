package Backendcom.example.Backend.repository;

import Backendcom.example.Backend.entity.ConfiguracionSunat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConfiguracionSunatRepository extends JpaRepository<ConfiguracionSunat, Long> {
    /**
     * Obtiene la configuración SUNAT de un usuario específico
     */
    Optional<ConfiguracionSunat> findByUsuarioIdUsuario(Long idUsuario);
    
    /**
     * Verifica si un usuario ya tiene configuración registrada
     */
    boolean existsByUsuarioIdUsuario(Long idUsuario);
}
