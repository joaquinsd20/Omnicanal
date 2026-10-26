package Backendcom.example.Backend.exception;

/**
 * Se lanza cuando un recurso solicitado (producto, venta, usuario, etc.)
 * no existe en la base de datos. El GlobalExceptionHandler la traduce a 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, String campo, Object valor) {
        super(String.format("%s no encontrado con %s = '%s'", recurso, campo, valor));
    }
}
