package Backendcom.example.Backend.repository;

import Backendcom.example.Backend.entity.Usuario;
import Backendcom.example.Backend.enums.EstadoUsuario;
import Backendcom.example.Backend.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByRuc(String ruc);

    List<Usuario> findByEstado(EstadoUsuario estado);

    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);

    List<Usuario> findByUsuarioCreador(Usuario usuarioCreador);

    @Query("SELECT u FROM Usuario u WHERE u.estado = 'ACTIVO'")
    List<Usuario> findActivos();

    @Query("SELECT u FROM Usuario u WHERE u.estado = 'BLOQUEADO'")
    List<Usuario> findBloqueados();

    @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario = 'ADMINISTRADOR_GLOBAL'")
    List<Usuario> findAdministradores();
}
