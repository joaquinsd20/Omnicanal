package Backendcom.example.Backend.controller;

import Backendcom.example.Backend.entity.Usuario;
import Backendcom.example.Backend.enums.TipoUsuario;
import Backendcom.example.Backend.exception.SecurityException;
import Backendcom.example.Backend.exception.ResourceNotFoundException;
import Backendcom.example.Backend.repository.UsuarioRepository;
import Backendcom.example.Backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * GET /api/usuarios - Obtener todos los usuarios (endpoint legado)
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    /**
     * GET /api/usuarios/{id} - Obtener usuario por ID (endpoint legado)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return ResponseEntity.ok(usuario);
    }

    /**
     * GET /api/usuarios/email/{email} - Obtener usuario por email (endpoint legado)
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> getUsuarioByEmail(@PathVariable String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
        return ResponseEntity.ok(usuario);
    }

    /**
     * DELETE /api/usuarios/{id} - Eliminar usuario (endpoint legado)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL')")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/usuarios/admin/todos - Obtener todos los usuarios (solo Admin)
     */
    @GetMapping("/admin/todos")
    @PreAuthorize("hasRole('ADMINISTRADOR_GLOBAL')")
    public ResponseEntity<List<Usuario>> obtenerTodos(
            @RequestHeader("X-User-Id") Long userId) {

        Usuario usuarioActual = obtenerUsuarioActual(userId);

        if (!usuarioActual.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR_GLOBAL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    /**
     * GET /api/usuarios/admin/activos - Obtener usuarios activos
     */
    @GetMapping("/admin/activos")
    @PreAuthorize("hasRole('ADMINISTRADOR_GLOBAL')")
    public ResponseEntity<List<Usuario>> obtenerActivos(
            @RequestHeader("X-User-Id") Long userId) {

        Usuario usuarioActual = obtenerUsuarioActual(userId);
        if (!usuarioActual.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR_GLOBAL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(usuarioService.obtenerActivos());
    }

    /**
     * GET /api/usuarios/admin/bloqueados - Obtener usuarios bloqueados
     */
    @GetMapping("/admin/bloqueados")
    @PreAuthorize("hasRole('ADMINISTRADOR_GLOBAL')")
    public ResponseEntity<List<Usuario>> obtenerBloqueados(
            @RequestHeader("X-User-Id") Long userId) {

        Usuario usuarioActual = obtenerUsuarioActual(userId);
        if (!usuarioActual.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR_GLOBAL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(usuarioService.obtenerBloqueados());
    }

    /**
     * GET /api/usuarios/admin/administradores - Obtener administradores globales
     */
    @GetMapping("/admin/administradores")
    @PreAuthorize("hasRole('ADMINISTRADOR_GLOBAL')")
    public ResponseEntity<List<Usuario>> obtenerAdministradores(
            @RequestHeader("X-User-Id") Long userId) {

        Usuario usuarioActual = obtenerUsuarioActual(userId);
        if (!usuarioActual.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR_GLOBAL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(usuarioService.obtenerAdministradores());
    }

    /**
     * POST /api/usuarios/admin/crear - Crear usuario
     */
    @PostMapping("/admin/crear")
    @PreAuthorize("hasRole('ADMINISTRADOR_GLOBAL')")
    public ResponseEntity<?> crearUsuario(
            @RequestBody Usuario usuario,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            Usuario usuarioActual = obtenerUsuarioActual(userId);
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario, usuarioActual);

            // Construir respuesta
            Map<String, Object> respuesta = Map.of(
                    "id_usuario", nuevoUsuario.getIdUsuario(),
                    "nombre_usuario", nuevoUsuario.getNombreUsuario(),
                    "email", nuevoUsuario.getEmail(),
                    "tipo_usuario", nuevoUsuario.getTipoUsuario().toString(),
                    "estado", nuevoUsuario.getEstado().toString(),
                    "mensaje", "Usuario creado exitosamente"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/usuarios/admin/{idUsuario}/bloquear - Bloquear usuario
     */
    @PutMapping("/admin/{idUsuario}/bloquear")
    @PreAuthorize("hasRole('ADMINISTRADOR_GLOBAL')")
    public ResponseEntity<?> bloquearUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            Usuario usuarioActual = obtenerUsuarioActual(userId);
            Usuario usuarioActualizado = usuarioService.bloquearUsuario(idUsuario, usuarioActual);

            return ResponseEntity.ok(usuarioActualizado);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/usuarios/admin/{idUsuario}/desbloquear - Desbloquear usuario
     */
    @PutMapping("/admin/{idUsuario}/desbloquear")
    @PreAuthorize("hasRole('ADMINISTRADOR_GLOBAL')")
    public ResponseEntity<?> desbloquearUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            Usuario usuarioActual = obtenerUsuarioActual(userId);
            Usuario usuarioActualizado = usuarioService.desbloquearUsuario(idUsuario, usuarioActual);

            return ResponseEntity.ok(usuarioActualizado);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/usuarios/admin/estadisticas - Obtener estadísticas
     */
    @GetMapping("/admin/estadisticas")
    @PreAuthorize("hasRole('ADMINISTRADOR_GLOBAL')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @RequestHeader("X-User-Id") Long userId) {

        Usuario usuarioActual = obtenerUsuarioActual(userId);
        if (!usuarioActual.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR_GLOBAL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(usuarioService.obtenerEstadisticas());
    }

    /**
     * Helper: Obtener usuario actual del header X-User-Id
     */
    private Usuario obtenerUsuarioActual(Long userId) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new SecurityException("Usuario no válido"));
    }
}
