package Backendcom.example.Backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio del dashboard global del administrador.
 *
 * Se usa JdbcTemplate para que el panel sea estable y no dependa de cargas LAZY
 * ni de relaciones JPA. Todos los datos salen directamente de la BD:
 * usuarios, planes, ventas, productos e importaciones.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private static final BigDecimal COMISION_PCT = new BigDecimal("10.00");
    private static final BigDecimal FACTOR_COMISION = new BigDecimal("0.10");
    private static final BigDecimal PRECIO_PLAN_EMPRENDEDOR = new BigDecimal("29.90");
    private static final BigDecimal PRECIO_PLAN_PRO = new BigDecimal("79.90");

    private final JdbcTemplate jdbcTemplate;

    public boolean esAdministradorGlobal(String email) {
        try {
            String tipo = jdbcTemplate.queryForObject(
                    "SELECT tipo_usuario FROM usuarios WHERE email = ? LIMIT 1",
                    String.class,
                    email
            );
            return "ADMINISTRADOR_GLOBAL".equalsIgnoreCase(tipo);
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> obtenerResumenGlobal() {
        LocalDate hoy = LocalDate.now();
        Timestamp inicioMes = Timestamp.valueOf(hoy.withDayOfMonth(1).atStartOfDay());
        Timestamp inicioDia = Timestamp.valueOf(hoy.atStartOfDay());
        Timestamp finDia = Timestamp.valueOf(hoy.plusDays(1).atStartOfDay());
        LocalDate inicioMesDate = hoy.withDayOfMonth(1);

        long totalUsuarios = count("SELECT COUNT(*) FROM usuarios");
        long usuariosActivos = count("SELECT COUNT(*) FROM usuarios WHERE estado = 'ACTIVO'");
        long usuariosBloqueados = count("SELECT COUNT(*) FROM usuarios WHERE estado = 'BLOQUEADO'");
        long administradores = count("SELECT COUNT(*) FROM usuarios WHERE tipo_usuario = 'ADMINISTRADOR_GLOBAL'");
        long emprendedoresActivos = count("SELECT COUNT(*) FROM usuarios WHERE tipo_usuario = 'EMPRENDEDOR' AND estado = 'ACTIVO'");

        long totalProductos = count("SELECT COUNT(*) FROM productos");
        long totalImportaciones = count("SELECT COUNT(*) FROM importaciones");
        long totalVentas = count("SELECT COUNT(*) FROM ventas");
        long ventasHoy = count("SELECT COUNT(*) FROM ventas WHERE fecha_venta >= ? AND fecha_venta < ?", inicioDia, finDia);

        BigDecimal ventasBrutasMes = money("SELECT COALESCE(SUM(total), 0) FROM ventas WHERE fecha_venta >= ?", inicioMes);
        BigDecimal ventasBrutasTotales = money("SELECT COALESCE(SUM(total), 0) FROM ventas");
        BigDecimal ticketPromedioGlobal = totalVentas == 0
                ? BigDecimal.ZERO
                : money("SELECT COALESCE(AVG(total), 0) FROM ventas WHERE fecha_venta >= ?", inicioMes);

        BigDecimal comisionesMes = ventasBrutasMes.multiply(FACTOR_COMISION).setScale(2, RoundingMode.HALF_UP);
        BigDecimal suscripcionesMes = calcularSuscripcionesMes();
        BigDecimal ingresosPlataformaMes = comisionesMes.add(suscripcionesMes).setScale(2, RoundingMode.HALF_UP);

        BigDecimal costoProductosVendidosMes = money("""
                SELECT COALESCE(SUM(p.precio_costo * dv.cantidad), 0)
                FROM detalles_ventas dv
                INNER JOIN ventas v ON v.id_venta = dv.id_venta
                INNER JOIN productos p ON p.id_producto = dv.id_producto
                WHERE v.fecha_venta >= ?
                """, inicioMes);

        BigDecimal costosImportacionesMes = money(
                "SELECT COALESCE(SUM(costo_total), 0) FROM importaciones WHERE fecha_factura >= ?",
                inicioMesDate
        );

        BigDecimal utilidadEmprendedoresMes = ventasBrutasMes
                .subtract(costoProductosVendidosMes)
                .subtract(costosImportacionesMes)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal valorInventarioGlobal = money(
                "SELECT COALESCE(SUM(precio_costo * stock_total), 0) FROM productos"
        );

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("mes", YearMonth.now().toString());
        resumen.put("totalUsuarios", totalUsuarios);
        resumen.put("usuariosActivos", usuariosActivos);
        resumen.put("usuariosBloqueados", usuariosBloqueados);
        resumen.put("administradores", administradores);
        resumen.put("emprendedoresActivos", emprendedoresActivos);
        resumen.put("totalProductos", totalProductos);
        resumen.put("totalImportaciones", totalImportaciones);
        resumen.put("totalVentas", totalVentas);
        resumen.put("ventasHoy", ventasHoy);
        resumen.put("ventasBrutasMes", ventasBrutasMes);
        resumen.put("ventasBrutasTotales", ventasBrutasTotales);
        resumen.put("comisionPlataformaPct", COMISION_PCT);
        resumen.put("comisionesMes", comisionesMes);
        resumen.put("suscripcionesMes", suscripcionesMes);
        resumen.put("ingresosPlataformaMes", ingresosPlataformaMes);
        resumen.put("utilidadEmprendedoresMes", utilidadEmprendedoresMes);
        resumen.put("valorInventarioGlobal", valorInventarioGlobal);
        resumen.put("ticketPromedioGlobal", ticketPromedioGlobal);
        resumen.put("suscripcionesDetalle", obtenerSuscripcionesDetalle());
        resumen.put("topEmprendedores", obtenerTopEmprendedores(inicioMes));
        resumen.put("ultimosUsuarios", obtenerUltimosUsuarios());
        resumen.put("ultimasVentas", obtenerUltimasVentas());
        return resumen;
    }

    private BigDecimal calcularSuscripcionesMes() {
        long planEmprendedor = count("""
                SELECT COUNT(*) FROM usuarios
                WHERE tipo_usuario = 'EMPRENDEDOR'
                  AND estado = 'ACTIVO'
                  AND plan_suscripcion = 'PLAN_EMPRENDEDOR'
                """);

        long planPro = count("""
                SELECT COUNT(*) FROM usuarios
                WHERE tipo_usuario = 'EMPRENDEDOR'
                  AND estado = 'ACTIVO'
                  AND plan_suscripcion = 'PLAN_PRO'
                """);

        return PRECIO_PLAN_EMPRENDEDOR.multiply(BigDecimal.valueOf(planEmprendedor))
                .add(PRECIO_PLAN_PRO.multiply(BigDecimal.valueOf(planPro)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<Map<String, Object>> obtenerSuscripcionesDetalle() {
        long planEmprendedor = count("""
                SELECT COUNT(*) FROM usuarios
                WHERE tipo_usuario = 'EMPRENDEDOR'
                  AND estado = 'ACTIVO'
                  AND plan_suscripcion = 'PLAN_EMPRENDEDOR'
                """);

        long planPro = count("""
                SELECT COUNT(*) FROM usuarios
                WHERE tipo_usuario = 'EMPRENDEDOR'
                  AND estado = 'ACTIVO'
                  AND plan_suscripcion = 'PLAN_PRO'
                """);

        return List.of(
                plan("PLAN_EMPRENDEDOR", planEmprendedor, PRECIO_PLAN_EMPRENDEDOR),
                plan("PLAN_PRO", planPro, PRECIO_PLAN_PRO)
        );
    }

    private Map<String, Object> plan(String nombre, long cantidad, BigDecimal precio) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("plan", nombre);
        fila.put("cantidad", cantidad);
        fila.put("precioUnitario", precio);
        fila.put("totalMensual", precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP));
        return fila;
    }

    private List<Map<String, Object>> obtenerTopEmprendedores(Timestamp inicioMes) {
        return jdbcTemplate.queryForList("""
                SELECT
                    u.id_usuario AS idUsuario,
                    u.nombre_usuario AS nombreUsuario,
                    u.email AS email,
                    u.plan_suscripcion AS plan,
                    COUNT(v.id_venta) AS ventas,
                    COALESCE(SUM(v.total), 0) AS totalVendido,
                    COALESCE(SUM(v.total), 0) * 0.10 AS comisionGenerada
                FROM usuarios u
                INNER JOIN ventas v ON v.id_usuario = u.id_usuario
                WHERE v.fecha_venta >= ?
                GROUP BY u.id_usuario, u.nombre_usuario, u.email, u.plan_suscripcion
                ORDER BY totalVendido DESC
                LIMIT 5
                """, inicioMes);
    }

    private List<Map<String, Object>> obtenerUltimosUsuarios() {
        return jdbcTemplate.queryForList("""
                SELECT
                    id_usuario AS idUsuario,
                    nombre_usuario AS nombreUsuario,
                    email,
                    tipo_usuario AS tipoUsuario,
                    plan_suscripcion AS planSuscripcion,
                    estado,
                    created_at AS createdAt
                FROM usuarios
                ORDER BY created_at DESC
                LIMIT 6
                """);
    }

    private List<Map<String, Object>> obtenerUltimasVentas() {
        return jdbcTemplate.queryForList("""
                SELECT
                    v.numero_venta AS numeroVenta,
                    v.fecha_venta AS fechaVenta,
                    COALESCE(u.nombre_usuario, 'Sin usuario') AS emprendedor,
                    v.nombre_cliente AS cliente,
                    v.total AS total,
                    v.total * 0.10 AS comision
                FROM ventas v
                LEFT JOIN usuarios u ON u.id_usuario = v.id_usuario
                ORDER BY v.fecha_venta DESC
                LIMIT 6
                """);
    }

    private long count(String sql, Object... args) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class, args);
        return value == null ? 0L : value.longValue();
    }

    private BigDecimal money(String sql, Object... args) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }
}
