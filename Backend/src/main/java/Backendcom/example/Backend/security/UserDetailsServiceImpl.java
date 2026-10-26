package Backendcom.example.Backend.security;

import Backendcom.example.Backend.entity.Usuario;
import Backendcom.example.Backend.enums.EstadoUsuario;
import Backendcom.example.Backend.enums.TipoUsuario;
import Backendcom.example.Backend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Carga los datos de autenticación a partir del email del usuario.
 * El "username" de Spring Security es el email (campo único en la BD).
 * La autoridad se deriva de tipoUsuario -> ROLE_<TIPO>.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No existe un usuario con el email: " + email));

        // Verificar que el usuario no esté bloqueado
        if (usuario.getEstado() != null && usuario.getEstado().equals(EstadoUsuario.BLOQUEADO)) {
            throw new UsernameNotFoundException("Usuario bloqueado por administrador.");
        }

        if (usuario.getEstado() != null && (usuario.getEstado().equals(EstadoUsuario.INACTIVO) || usuario.getEstado().equals(EstadoUsuario.SUSPENDIDO))) {
            throw new UsernameNotFoundException("La cuenta está inactiva o suspendida.");
        }

        String rol = (usuario.getTipoUsuario() == null ? TipoUsuario.EMPRENDEDOR : usuario.getTipoUsuario()).name();

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + rol)))
                .build();
    }
}
