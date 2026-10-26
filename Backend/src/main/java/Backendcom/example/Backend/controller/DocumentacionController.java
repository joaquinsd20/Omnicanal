package Backendcom.example.Backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Documentación simple de endpoints REST.
 * Cumple el punto del sílabo de documentación de API sin agregar dependencias extra.
 * URL pública con context-path: GET /api/documentacion
 */
@RestController
@RequestMapping("/documentacion")
public class DocumentacionController {

    @GetMapping
    public Map<String, Object> documentacion() {
        return Map.of(
                "nombre", "Sistema Web de Gestión Omnicanal e Importaciones",
                "version", "1.0",
                "seguridad", "JWT Bearer Token. Enviar Authorization: Bearer <token> y X-User-Id.",
                "modulos", List.of(
                        Map.of("modulo", "Auth", "endpoints", List.of("POST /api/auth/login", "POST /api/auth/register")),
                        Map.of("modulo", "Productos", "endpoints", List.of("GET /api/productos", "GET /api/productos/page", "GET /api/productos/buscar/entity-manager", "POST /api/productos", "PUT /api/productos/{id}", "DELETE /api/productos/{id}")),
                        Map.of("modulo", "Ventas", "endpoints", List.of("GET /api/ventas", "GET /api/ventas/page", "POST /api/ventas?simularError=false", "GET /api/ventas/resumen/por-canal")),
                        Map.of("modulo", "Importaciones", "endpoints", List.of("GET /api/importaciones", "POST /api/importaciones?simularError=false", "GET /api/importaciones/{id}")),
                        Map.of("modulo", "Kardex", "endpoints", List.of("GET /api/kardex", "GET /api/kardex/producto/{idProducto}", "GET /api/kardex/tipo/{tipoMovimiento}")),
                        Map.of("modulo", "Dashboard", "endpoints", List.of("GET /api/dashboard/resumen", "GET /api/dashboard/resumen-diario", "GET /api/dashboard/tendencia-7-dias", "GET /api/dashboard/inventario", "GET /api/dashboard/productos-top")),
                        Map.of("modulo", "Administración", "endpoints", List.of("GET /api/usuarios/admin/todos", "POST /api/usuarios/admin/crear", "PUT /api/usuarios/admin/{id}/bloquear", "PUT /api/usuarios/admin/{id}/desbloquear"))
                ),
                "temasCurso", List.of("Angular", "Routing", "HTTP", "CORS", "Spring API REST", "JPA", "JpaRepository", "JPQL", "EntityManager", "JWT", "@Transactional", "Rollback", "Paginación")
        );
    }
}
