package Backendcom.example.Backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cuerpo JSON estandarizado para todas las respuestas de error.
 * Ejemplo:
 * {
 *   "timestamp": "2026-06-21T10:30:00",
 *   "status": 400,
 *   "error": "Validación fallida",
 *   "message": "Revisa los campos enviados",
 *   "path": "/api/productos",
 *   "errores": { "precioVenta": "debe ser mayor que 0" }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> errores;   // errores de validación campo -> mensaje
}
