# Documentación rápida de API - Omnicanal

Con el backend encendido, abrir:

- `GET http://localhost:8080/api/documentacion`

La API usa JWT. Después del login se debe enviar:

- `Authorization: Bearer <token>`
- `X-User-Id: <idUsuario>`

## Endpoints clave

- Auth: `POST /api/auth/login`, `POST /api/auth/register`
- Productos: `GET /api/productos`, `GET /api/productos/page`, `GET /api/productos/buscar/entity-manager`
- Ventas: `GET /api/ventas/page`, `POST /api/ventas?simularError=true`
- Importaciones: `POST /api/importaciones?simularError=true`
- Dashboard: `GET /api/dashboard/resumen`, `GET /api/dashboard/resumen-diario`, `GET /api/dashboard/tendencia-7-dias`
- Admin: `GET /api/usuarios/admin/todos`, `POST /api/usuarios/admin/crear`

## Evidencias del sílabo cubiertas

- Angular + servicios HTTP + routing.
- Spring Boot API REST.
- JPA, JpaRepository, JPQL con parámetros nombrados.
- `@Repository` + `EntityManager` en `ReporteRepository`.
- `@Transactional` y rollback simulado en ventas/importaciones.
- Paginación con `Page`, `Pageable` y `PageRequest`.
- Seguridad con Spring Security y JWT.
