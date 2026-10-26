package Backendcom.example.Backend.controller;

import Backendcom.example.Backend.entity.Canal;
import Backendcom.example.Backend.entity.Usuario;
import Backendcom.example.Backend.repository.CanalRepository;
import Backendcom.example.Backend.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/canales")
@RequiredArgsConstructor
public class CanalController {

    private final CanalRepository canalRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Obtener todos los canales activos del usuario
     * GET /api/canales/usuario/{idUsuario}
     */
    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<List<Canal>> obtenerCanalesDelUsuario(@PathVariable Long idUsuario) {
        try {
            List<Canal> canales = canalRepository.findByUsuarioIdUsuarioAndEstado(idUsuario, "activo");
            return ResponseEntity.ok(canales);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener canal por ID
     * GET /api/canales/{idCanal}
     */
    @GetMapping("/{idCanal}")
    public ResponseEntity<Canal> obtenerCanal(@PathVariable Long idCanal) {
        try {
            Optional<Canal> canal = canalRepository.findById(idCanal);
            return canal.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crear nuevo canal
     * POST /api/canales
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Canal> crearCanal(@Valid @RequestBody Canal canal, @RequestHeader("X-User-Id") Long idUsuario) {
        try {
            Optional<Usuario> usuario = usuarioRepository.findById(idUsuario);
            if (usuario.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            canal.setUsuario(usuario.get());
            canal.setEstado("activo");
            
            Canal canalGuardado = canalRepository.save(canal);
            return ResponseEntity.status(HttpStatus.CREATED).body(canalGuardado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar canal
     * PUT /api/canales/{idCanal}
     */
    @PutMapping("/{idCanal}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Canal> actualizarCanal(
            @PathVariable Long idCanal,
            @Valid @RequestBody Canal canalActualizado,
            @RequestHeader("X-User-Id") Long idUsuario) {
        try {
            Optional<Canal> canalExistente = canalRepository.findById(idCanal);
            
            if (canalExistente.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            Canal canal = canalExistente.get();
            
            // Validar que el usuario sea propietario del canal
            if (!canal.getUsuario().getIdUsuario().equals(idUsuario)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Actualizar campos
            if (canalActualizado.getNombreCanal() != null) {
                canal.setNombreCanal(canalActualizado.getNombreCanal());
            }
            if (canalActualizado.getTipoCanal() != null) {
                canal.setTipoCanal(canalActualizado.getTipoCanal());
            }
            if (canalActualizado.getUrlTienda() != null) {
                canal.setUrlTienda(canalActualizado.getUrlTienda());
            }
            if (canalActualizado.getEstado() != null) {
                canal.setEstado(canalActualizado.getEstado());
            }
            
            Canal canalActualizado2 = canalRepository.save(canal);
            return ResponseEntity.ok(canalActualizado2);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar canal
     * DELETE /api/canales/{idCanal}
     */
    @DeleteMapping("/{idCanal}")
    @PreAuthorize("hasAnyRole('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    public ResponseEntity<Void> eliminarCanal(
            @PathVariable Long idCanal,
            @RequestHeader("X-User-Id") Long idUsuario) {
        try {
            Optional<Canal> canal = canalRepository.findById(idCanal);
            
            if (canal.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // Validar que el usuario sea propietario del canal
            if (!canal.get().getUsuario().getIdUsuario().equals(idUsuario)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            canalRepository.deleteById(idCanal);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
