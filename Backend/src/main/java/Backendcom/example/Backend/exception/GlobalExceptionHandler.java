package Backendcom.example.Backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Captura las excepciones lanzadas por los controladores y las traduce a
 * respuestas JSON limpias y consistentes para el frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----- 400: errores de validación de @Valid en DTOs/entidades -----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                      HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validación fallida")
                .message("Revisa los campos enviados")
                .path(request.getRequestURI())
                .errores(errores)
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    // ----- 404: recurso no encontrado -----
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                   HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request);
    }

    // ----- 400: reglas de negocio -----
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex,
                                                   HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Error de negocio", ex.getMessage(), request);
    }

    // ----- 409: violación de integridad (p. ej. email/sku/ruc duplicado) -----
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex,
                                                     HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflicto de datos",
                "El registro viola una restricción única o de integridad (¿valor duplicado?).",
                request);
    }

    // ----- 401: credenciales inválidas -----
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex,
                                               HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "No autorizado",
                "Email o contraseña incorrectos.", request);
    }

    // ----- 400: argumento inválido -----
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage(), request);
    }

    // ----- 500: cualquier otra excepción no controlada -----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado en el servidor.", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error,
                                           String message, HttpServletRequest request) {
        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
