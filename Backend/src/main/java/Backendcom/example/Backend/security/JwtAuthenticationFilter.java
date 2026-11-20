package Backendcom.example.Backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filtro que se ejecuta una vez por petición. Extrae el token del header
 * Authorization (Bearer ...), lo valida y, si es correcto, inyecta el
 * contexto de seguridad para que el resto de la petición se considere
 * autenticada.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    /** Rutas tipo /productos/usuario/{id}: solo el propio usuario o un administrador. */
    private static final Pattern RUTA_POR_USUARIO = Pattern.compile("/(productos|ventas|canales)/usuario/(\\d+)");
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Sin header Bearer -> dejamos pasar; la cadena de seguridad decidirá.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        UsuarioPrincipal principalAutenticado = null;

        try {
            final String email = jwtService.extractUsername(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    if (userDetails instanceof UsuarioPrincipal principal) {
                        principalAutenticado = principal;
                    }
                } else {
                    logger.warn("JWT Filter: token inválido");
                }
            }
        } catch (JwtException | IllegalArgumentException
                 | org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
            // Token inválido/expirado/manipulado o usuario bloqueado: no autenticamos.
            // La respuesta 401 la produce el EntryPoint configurado en SecurityConfig.
            logger.debug("JWT Filter: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        if (principalAutenticado == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!puedeAccederRutaDeUsuario(request, principalAutenticado)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No puedes acceder a datos de otro usuario.");
            return;
        }

        // El id del usuario siempre sale del token, nunca del cliente.
        filterChain.doFilter(new UserIdHeaderRequestWrapper(request, principalAutenticado.getIdUsuario()), response);
    }

    private boolean puedeAccederRutaDeUsuario(HttpServletRequest request, UsuarioPrincipal principal) {
        Matcher m = RUTA_POR_USUARIO.matcher(request.getRequestURI());
        if (!m.find()) {
            return true;
        }
        boolean esAdmin = principal.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMINISTRADOR") || a.getAuthority().equals("ROLE_ADMINISTRADOR_GLOBAL"));
        return esAdmin || m.group(2).equals(String.valueOf(principal.getIdUsuario()));
    }
}
