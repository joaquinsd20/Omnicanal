package Backendcom.example.Backend.repository;

import Backendcom.example.Backend.entity.Canal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CanalRepository extends JpaRepository<Canal, Long> {
    List<Canal> findByUsuarioIdUsuario(Long idUsuario);
    List<Canal> findByUsuarioIdUsuarioAndEstado(Long idUsuario, String estado);
}
