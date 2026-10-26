package Backendcom.example.Backend.exception;

/**
 * Excepción para violaciones de reglas de negocio
 * (p. ej. "el stock no puede ser negativo", "el SKU ya existe").
 * El GlobalExceptionHandler la traduce a 400 Bad Request.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
