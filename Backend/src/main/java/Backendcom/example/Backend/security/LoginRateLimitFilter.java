package Backendcom.example.Backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Limita los intentos de login y registro por IP (ventana deslizante en memoria).
 * Al superar el límite responde 429 Too Many Requests.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final int maxIntentos;
    private final Duration ventana;
    private final Map<String, Deque<Instant>> intentosPorIp = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(@Value("${app.security.login-max-attempts:5}") int maxIntentos,
                                @Value("${app.security.login-window-minutes:15}") long ventanaMinutos) {
        this.maxIntentos = maxIntentos;
        this.ventana = Duration.ofMinutes(ventanaMinutos);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !(uri.endsWith("/auth/login") || uri.endsWith("/auth/register"));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Instant ahora = Instant.now();
        Deque<Instant> intentos = intentosPorIp.computeIfAbsent(request.getRemoteAddr(), k -> new ConcurrentLinkedDeque<>());
        while (!intentos.isEmpty() && intentos.peekFirst().isBefore(ahora.minus(ventana))) {
            intentos.pollFirst();
        }
        if (intentos.size() >= maxIntentos) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(ventana.toSeconds()));
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":429,\"message\":\"Demasiados intentos. Espera unos minutos e inténtalo de nuevo.\"}");
            return;
        }
        intentos.addLast(ahora);
        filterChain.doFilter(request, response);

        // Un login correcto limpia el contador de esa IP
        if (response.getStatus() < 400) {
            intentos.clear();
        }
        if (intentosPorIp.size() > 10_000) {
            intentosPorIp.entrySet().removeIf(e -> e.getValue().isEmpty());
        }
    }
}
