package Backendcom.example.Backend.service;

import Backendcom.example.Backend.dto.AuthResponse;
import Backendcom.example.Backend.dto.LoginRequest;
import Backendcom.example.Backend.dto.RegisterRequest;
import Backendcom.example.Backend.entity.Usuario;
import Backendcom.example.Backend.enums.EstadoUsuario;
import Backendcom.example.Backend.enums.TipoUsuario;
import Backendcom.example.Backend.enums.PlanSuscripcion;
import Backendcom.example.Backend.exception.BusinessException;
import Backendcom.example.Backend.repository.UsuarioRepository;
import Backendcom.example.Backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /** Registra un nuevo usuario, cifra su contraseña y devuelve un token. */
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Ya existe una cuenta con ese email.");
        }
        if (request.getRuc() != null && !request.getRuc().isBlank()
                && usuarioRepository.findByRuc(request.getRuc()).isPresent()) {
            throw new BusinessException("Ya existe una cuenta con ese RUC.");
        }

        // NUEVO: Usar tipoUsuario del request o EMPRENDEDOR por defecto
        TipoUsuario tipoUsuario = TipoUsuario.EMPRENDEDOR;
        if (request.getTipoUsuario() != null && !request.getTipoUsuario().isBlank()) {
            try {
                tipoUsuario = TipoUsuario.valueOf(request.getTipoUsuario().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Tipo de usuario inválido: " + request.getTipoUsuario());
            }
        }

        Usuario usuario = Usuario.builder()
                .nombreUsuario(request.getNombreUsuario())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .razonSocial(request.getRazonSocial())
                .ruc(request.getRuc() == null || request.getRuc().isBlank() ? null : request.getRuc())
                .ciudad(request.getCiudad())
                .pais("Perú")
                .tipoUsuario(tipoUsuario)
                .planSuscripcion(PlanSuscripcion.PLAN_EMPRENDEDOR)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return buildResponse(guardado);
    }

    /** Verifica credenciales con el AuthenticationManager y devuelve un token. */
    public AuthResponse login(LoginRequest request) {
        // Lanza BadCredentialsException si el email/contraseña no coinciden.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));

        // NUEVO: Verificar si el usuario está bloqueado
        if (usuario.getEstado() != null && usuario.getEstado().equals(EstadoUsuario.BLOQUEADO)) {
            throw new BusinessException("Usuario bloqueado por administrador. Contacte al soporte.");
        }

        return buildResponse(usuario);
    }

    private AuthResponse buildResponse(Usuario usuario) {
        String rol = (usuario.getTipoUsuario() == null ? TipoUsuario.EMPRENDEDOR : usuario.getTipoUsuario()).name().toUpperCase();
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities("ROLE_" + rol)
                .build();

        String token = jwtService.generateToken(
                Map.of("idUsuario", usuario.getIdUsuario(), "rol", rol),
                userDetails);

        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .expiraEnMs(jwtService.getExpirationMillis())
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombreUsuario())
                .email(usuario.getEmail())
                .tipoUsuario(usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().name() : "EMPRENDEDOR")
                .planSuscripcion(usuario.getPlanSuscripcion() != null ? usuario.getPlanSuscripcion().name() : "PLAN_EMPRENDEDOR")
                .estado(usuario.getEstado() != null ? usuario.getEstado().name() : "ACTIVO")
                .build();
    }
}
