package Backendcom.example.Backend.service;

import Backendcom.example.Backend.dto.DashboardResumenDTO;
import Backendcom.example.Backend.dto.VentasPorCanalDTO;
import Backendcom.example.Backend.dto.ProductoTopDTO;
import Backendcom.example.Backend.entity.*;
import Backendcom.example.Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ImportacionRepository importacionRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    /**
     * Resumen financiero del mes actual del usuario.
     * Este método alimenta el panel financiero real desde la BD.
     */
    public DashboardResumenDTO obtenerResumenFinanciero(Long idUsuario) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now();

        List<Venta> ventasDelMes = ventasEntreFechas(idUsuario, inicioMes, finMes);

        BigDecimal totalIngresos = ventasDelMes.stream()
                .map(v -> nvl(v.getTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int cantidadVentas = ventasDelMes.size();
        BigDecimal ticketPromedio = cantidadVentas > 0
                ? totalIngresos.divide(BigDecimal.valueOf(cantidadVentas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Importacion> importacionesDelMes = importacionRepository.findByUsuarioIdUsuario(idUsuario).stream()
                .filter(i -> i.getFechaFactura() != null
                        && !i.getFechaFactura().isBefore(inicioMes)
                        && !i.getFechaFactura().isAfter(finMes))
                .collect(Collectors.toList());

        BigDecimal totalCostoImportaciones = importacionesDelMes.stream()
                .map(i -> nvl(i.getCostoTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCostoEnvios = ventasDelMes.stream()
                .map(v -> nvl(v.getCostoEnvio()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal costoProductosVendidos = calcularCostoProductosVendidos(ventasDelMes);
        BigDecimal totalCostos = totalCostoImportaciones.add(totalCostoEnvios).add(costoProductosVendidos);
        BigDecimal gananciaNeta = totalIngresos.subtract(totalCostos);
        BigDecimal margenNetoPct = porcentaje(gananciaNeta, totalIngresos);

        List<Producto> productos = productoRepository.findByUsuarioIdUsuario(idUsuario);
        BigDecimal valorInventario = productos.stream()
                .map(p -> nvl(p.getPrecioCosto()).multiply(BigDecimal.valueOf(p.getStockTotal() == null ? 0 : p.getStockTotal())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardResumenDTO.builder()
                .totalIngresos(totalIngresos)
                .totalVentas(cantidadVentas)
                .ticketPromedio(ticketPromedio)
                .totalCostos(totalCostos)
                .costoImportaciones(totalCostoImportaciones)
                .costoEnvios(totalCostoEnvios)
                .gananciaNeta(gananciaNeta)
                .margenNetoPct(margenNetoPct)
                .valorInventario(valorInventario)
                .mes(YearMonth.now().toString())
                .build();
    }

    /** Ingresos desagregados por canal de venta. */
    public List<VentasPorCanalDTO> obtenerVentasPorCanal(Long idUsuario) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now();
        List<Venta> ventasDelMes = ventasEntreFechas(idUsuario, inicioMes, finMes);
        BigDecimal totalMes = ventasDelMes.stream().map(v -> nvl(v.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, List<Venta>> ventasPorCanal = ventasDelMes.stream()
                .collect(Collectors.groupingBy(v -> v.getCanal() != null ? v.getCanal().getNombreCanal() : "Sin canal"));

        return ventasPorCanal.entrySet().stream()
                .map(entry -> {
                    BigDecimal totalCanal = entry.getValue().stream()
                            .map(v -> nvl(v.getTotal()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return VentasPorCanalDTO.builder()
                            .nombreCanal(entry.getKey())
                            .cantidadVentas(entry.getValue().size())
                            .totalVentas(totalCanal)
                            .porcentajeDelTotal(porcentaje(totalCanal, totalMes))
                            .build();
                })
                .sorted(Comparator.comparing(VentasPorCanalDTO::getTotalVentas).reversed())
                .collect(Collectors.toList());
    }

    /** Top productos más vendidos del mes, calculado desde detalles_ventas. */
    public List<ProductoTopDTO> obtenerProductosTop(Long idUsuario, int limite) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now();
        List<Venta> ventasDelMes = ventasEntreFechas(idUsuario, inicioMes, finMes);
        Set<Long> idsVentas = ventasDelMes.stream().map(Venta::getIdVenta).collect(Collectors.toSet());

        Map<Long, AcumuladoProducto> acumulado = new HashMap<>();
        for (DetalleVenta detalle : detalleVentaRepository.findAll()) {
            if (detalle.getVenta() == null || !idsVentas.contains(detalle.getVenta().getIdVenta())) {
                continue;
            }
            Producto producto = detalle.getProducto();
            if (producto == null) {
                continue;
            }
            AcumuladoProducto acc = acumulado.computeIfAbsent(producto.getIdProducto(), id -> new AcumuladoProducto(producto));
            int cantidad = detalle.getCantidad() == null ? 0 : detalle.getCantidad();
            BigDecimal ingreso = nvl(detalle.getSubtotal());
            if (ingreso.compareTo(BigDecimal.ZERO) == 0) {
                ingreso = nvl(detalle.getPrecioUnitario()).multiply(BigDecimal.valueOf(cantidad));
            }
            acc.cantidadVendida += cantidad;
            acc.ingresos = acc.ingresos.add(ingreso);
        }

        return acumulado.values().stream()
                .sorted(Comparator.comparing((AcumuladoProducto a) -> a.ingresos).reversed())
                .limit(limite)
                .map(a -> ProductoTopDTO.builder()
                        .idProducto(a.producto.getIdProducto())
                        .nombreProducto(a.producto.getNombreProducto())
                        .precioCosto(a.producto.getPrecioCosto())
                        .precioVenta(a.producto.getPrecioVenta())
                        .stockActual(a.producto.getStockTotal())
                        .margenGanancia(a.producto.getMargenGananciaPct())
                        .cantidadVendida(a.cantidadVendida)
                        .ingresos(a.ingresos)
                        .build())
                .collect(Collectors.toList());
    }

    /** Resumen de inventario y alertas de stock bajo. */
    public Map<String, Object> obtenerResumenInventario(Long idUsuario) {
        List<Producto> productos = productoRepository.findByUsuarioIdUsuario(idUsuario);
        List<Producto> stockBajo = productos.stream()
                .filter(p -> p.getStockTotal() != null && p.getStockMinimo() != null && p.getStockTotal() <= p.getStockMinimo())
                .collect(Collectors.toList());
        List<Producto> sinStock = productos.stream()
                .filter(p -> p.getStockTotal() != null && p.getStockTotal() == 0)
                .collect(Collectors.toList());
        BigDecimal valorTotal = productos.stream()
                .map(p -> nvl(p.getPrecioCosto()).multiply(BigDecimal.valueOf(p.getStockTotal() == null ? 0 : p.getStockTotal())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalProductos", productos.size());
        resumen.put("productosConStockBajo", stockBajo.size());
        resumen.put("productosSinStock", sinStock.size());
        resumen.put("valorTotalInventario", valorTotal);
        resumen.put("productosAlertaDetalle", stockBajo.stream()
                .map(p -> Map.of(
                        "nombre", p.getNombreProducto() == null ? "Producto" : p.getNombreProducto(),
                        "stockActual", p.getStockTotal() == null ? 0 : p.getStockTotal(),
                        "stockMinimo", p.getStockMinimo() == null ? 0 : p.getStockMinimo()))
                .collect(Collectors.toList()));
        return resumen;
    }

    /** Ventas de los últimos 7 días para el gráfico del frontend. */
    public List<Map<String, Object>> obtenerTendenciaUltimos7Dias(Long idUsuario) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusDays(6);
        List<Venta> ventas = ventasEntreFechas(idUsuario, inicio, hoy);

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicio.plusDays(i);
            BigDecimal ingresosDia = ventas.stream()
                    .filter(v -> v.getFechaVenta() != null && v.getFechaVenta().toLocalDate().equals(dia))
                    .map(v -> nvl(v.getTotal()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("fecha", dia.equals(hoy) ? "Hoy" : dia.getDayOfWeek().toString().substring(0, 3));
            fila.put("fechaIso", dia.toString());
            fila.put("ingresos", ingresosDia);
            resultado.add(fila);
        }
        return resultado;
    }

    /** Resumen real del día actual. */
    public Map<String, Object> obtenerResumenDiario(Long idUsuario) {
        LocalDate hoy = LocalDate.now();
        List<Venta> ventasHoy = ventasEntreFechas(idUsuario, hoy, hoy);
        BigDecimal ingresos = ventasHoy.stream().map(v -> nvl(v.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costosProductos = calcularCostoProductosVendidos(ventasHoy);
        BigDecimal costosEnvio = ventasHoy.stream().map(v -> nvl(v.getCostoEnvio())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costos = costosProductos.add(costosEnvio);

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("ingresos", ingresos);
        resumen.put("costos", costos);
        resumen.put("ganancia", ingresos.subtract(costos));
        resumen.put("ventasHoy", ventasHoy.size());
        resumen.put("productosVendidos", contarProductosVendidos(ventasHoy));
        return resumen;
    }

    private List<Venta> ventasEntreFechas(Long idUsuario, LocalDate inicio, LocalDate finInclusive) {
        LocalDateTime desde = inicio.atStartOfDay();
        LocalDateTime hasta = finInclusive.plusDays(1).atStartOfDay();
        return ventaRepository.findByUsuarioIdUsuario(idUsuario).stream()
                .filter(v -> v.getFechaVenta() != null && !v.getFechaVenta().isBefore(desde) && v.getFechaVenta().isBefore(hasta))
                .collect(Collectors.toList());
    }

    private BigDecimal calcularCostoProductosVendidos(List<Venta> ventas) {
        Set<Long> idsVentas = ventas.stream().map(Venta::getIdVenta).collect(Collectors.toSet());
        return detalleVentaRepository.findAll().stream()
                .filter(d -> d.getVenta() != null && idsVentas.contains(d.getVenta().getIdVenta()))
                .map(d -> {
                    Producto p = d.getProducto();
                    int cantidad = d.getCantidad() == null ? 0 : d.getCantidad();
                    return p == null ? BigDecimal.ZERO : nvl(p.getPrecioCosto()).multiply(BigDecimal.valueOf(cantidad));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int contarProductosVendidos(List<Venta> ventas) {
        Set<Long> idsVentas = ventas.stream().map(Venta::getIdVenta).collect(Collectors.toSet());
        return detalleVentaRepository.findAll().stream()
                .filter(d -> d.getVenta() != null && idsVentas.contains(d.getVenta().getIdVenta()))
                .mapToInt(d -> d.getCantidad() == null ? 0 : d.getCantidad())
                .sum();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        return total.compareTo(BigDecimal.ZERO) > 0
                ? parte.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    private static class AcumuladoProducto {
        private final Producto producto;
        private int cantidadVendida = 0;
        private BigDecimal ingresos = BigDecimal.ZERO;

        private AcumuladoProducto(Producto producto) {
            this.producto = producto;
        }
    }
}
