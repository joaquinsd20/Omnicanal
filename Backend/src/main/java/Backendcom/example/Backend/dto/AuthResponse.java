package Backendcom.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta devuelta tras un login o registro exitoso.
 * Incluye el token y los datos básicos del usuario (sin la contraseña).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tipo;            // "Bearer"
    private long expiraEnMs;
    private Long idUsuario;
    private String nombreUsuario;
    private String email;
    private String tipoUsuario;
    private String planSuscripcion;
    private String estado;          // NUEVO: Para verificar si está bloqueado
}
