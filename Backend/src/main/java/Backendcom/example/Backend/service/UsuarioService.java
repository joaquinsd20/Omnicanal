package Backendcom.example.Backend.service;

import Backendcom.example.Backend.entity.Usuario;
import Backendcom.example.Backend.enums.EstadoUsuario;
import Backendcom.example.Backend.enums.TipoUsuario;
import Backendcom.example.Backend.exception.SecurityException;
import Backendcom.example.Backend.exception.EntityNotFoundException;
import Backendcom.example.Backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtener todos los usuarios
     */
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtener usuarios activos
     */
    public List<Usuario> obtenerActivos() {
        return usuarioRepository.findActivos();
    }

    /**
     * Obtener usuarios bloqueados
     */
    public List<Usuario> obtenerBloqueados() {
        return usuarioRepository.findBloqueados();
    }

    /**
     * Obtener administradores globales
     */
    public List<Usuario> obtenerAdministradores() {
        return usuarioRepository.findAdministradores();
    }

    /**
     * Obtener usuario por ID
     */
    public Usuario obtenerPorId(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }

    /**
     * Crear usuario (solo OG puede hacer esto)
     */
    public Usuario crearUsuario(Usuario usuario, Usuario creadorActual) {
        // Validar que quien crea sea admin
        if (!creadorActual.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR_GLOBAL)) {
            throw new SecurityException("Solo administradores globales pueden crear usuarios");
        }

        // Validar email único
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email ya registrado: " + usuario.getEmail());
        }

        // Generar contraseña si no viene
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            usuario.setPassword(generarContraseñaTemporal());
        }

        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Asignar creador
        usuario.setUsuarioCreador(creadorActual);

        // Estado por defecto: activo
        usuario.setEstado(EstadoUsuario.ACTIVO);

        // Tipo por defecto: emprendedor
        if (usuario.getTipoUsuario() == null) {
            usuario.setTipoUsuario(TipoUsuario.EMPRENDEDOR);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Bloquear usuario (solo OG)
     */
    public Usuario bloquearUsuario(Long idUsuario, Usuario usuarioActual) {
        // Validar permisos
        if (!usuarioActual.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR_GLOBAL)) {
            throw new SecurityException("No autorizado para bloquear usuarios");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        usuario.setEstado(EstadoUsuario.BLOQUEADO);
        return usuarioRepository.save(usuario);
    }

    /**
     * Desbloquear usuario (solo OG)
     */
    public Usuario desbloquearUsuario(Long idUsuario, Usuario usuarioActual) {
        if (!usuarioActual.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR_GLOBAL)) {
            throw new SecurityException("No autorizado para desbloquear usuarios");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        usuario.setEstado(EstadoUsuario.ACTIVO);
        return usuarioRepository.save(usuario);
    }

    /**
     * Obtener estadísticas de usuarios
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = usuarioRepository.count();
        long activos = usuarioRepository.findActivos().size();
        long bloqueados = usuarioRepository.findBloqueados().size();
        long administradores = usuarioRepository.findAdministradores().size();

        stats.put("total_usuarios", total);
        stats.put("usuarios_activos", activos);
        stats.put("usuarios_bloqueados", bloqueados);
        stats.put("administradores", administradores);
        stats.put("porcentaje_activos", total > 0 ? (activos * 100 / total) : 0);
        stats.put("porcentaje_bloqueados", total > 0 ? (bloqueados * 100 / total) : 0);

        return stats;
    }

    /**
     * Generar contraseña temporal
     */
    private String generarContraseñaTemporal() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            password.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return password.toString();
    }
}
