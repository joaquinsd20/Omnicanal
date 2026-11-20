package Backendcom.example.Backend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Usuario autenticado con su id de base de datos.
 * Permite que el backend use el id del token y no el que envía el cliente.
 */
public class UsuarioPrincipal extends User {

    private final Long idUsuario;

    public UsuarioPrincipal(Long idUsuario, String email, String password,
                            Collection<? extends GrantedAuthority> authorities) {
        super(email, password, authorities);
        this.idUsuario = idUsuario;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }
}
