package Backendcom.example.Backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Reemplaza el header X-User-Id por el id del usuario autenticado en el JWT.
 * Así un cliente no puede leer ni modificar datos de otro usuario cambiando ese header.
 */
public class UserIdHeaderRequestWrapper extends HttpServletRequestWrapper {

    public static final String HEADER = "X-User-Id";
    private final String userId;

    public UserIdHeaderRequestWrapper(HttpServletRequest request, Long userId) {
        super(request);
        this.userId = String.valueOf(userId);
    }

    @Override
    public String getHeader(String name) {
        return HEADER.equalsIgnoreCase(name) ? userId : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return HEADER.equalsIgnoreCase(name)
                ? Collections.enumeration(List.of(userId))
                : super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = new ArrayList<>();
        Enumeration<String> original = super.getHeaderNames();
        while (original.hasMoreElements()) {
            String n = original.nextElement();
            if (!HEADER.equalsIgnoreCase(n)) {
                names.add(n);
            }
        }
        names.add(HEADER);
        return Collections.enumeration(names);
    }
}
