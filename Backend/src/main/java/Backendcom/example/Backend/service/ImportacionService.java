package Backendcom.example.Backend.service;

import Backendcom.example.Backend.dto.*;
import Backendcom.example.Backend.entity.*;
import Backendcom.example.Backend.exception.ResourceNotFoundException;
import Backendcom.example.Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportacionService {

    private final ImportacionRepository importacionRepository;
    private final ProrrateoImportacionRepository prorrateoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioService movimientoInventarioService;

    /**
     * Crea una nueva importación y distribuye los costos entre los productos (prorrateo)
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportacionResponse crearImportacionConProrrateo(Long idUsuario, CrearImportacionRequest request, boolean simularError) {
        
        // 1. Validar usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Verificar que no exista factura duplicada
        if (importacionRepository.findByNumeroFactura(request.getNumeroFactura()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una importación con el número de factura: " + request.getNumeroFactura());
        }

        // 3. Validar que todos los productos existan
        for (ItemProrrateoRequest item : request.getItems()) {
            productoRepository.findById(item.getIdProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + item.getIdProducto()));
        }

        // 4. Calcular costos totales
        BigDecimal costoFlete = request.getCostoFlete() != null ? request.getCostoFlete() : BigDecimal.ZERO;
        BigDecimal costoAduanas = request.getCostoAduanas() != null ? request.getCostoAduanas() : BigDecimal.ZERO;
        BigDecimal costoAranceles = request.getCostoAranceles() != null ? request.getCostoAranceles() : BigDecimal.ZERO;
        BigDecimal costoTransporte = request.getCostoTransporteLocal() != null ? request.getCostoTransporteLocal() : BigDecimal.ZERO;
        BigDecimal otrosCostos = request.getOtrosCostos() != null ? request.getOtrosCostos() : BigDecimal.ZERO;

        BigDecimal costoTotal = costoFlete.add(costoAduanas).add(costoAranceles).add(costoTransporte).add(otrosCostos);

        // 5. Convertir monto original a soles
        BigDecimal montoEnSoles = request.getMontoOriginal().multiply(request.getTipoCambio())
            .setScale(2, RoundingMode.HALF_UP);

        // 6. Crear la entidad Importacion
        Importacion importacion = Importacion.builder()
            .usuario(usuario)
            .numeroFactura(request.getNumeroFactura())
            .fechaFactura(request.getFechaFactura())
            .proveedorInternacional(request.getProveedorInternacional())
            .paisOrigen(request.getPaisOrigen())
            .montoOriginal(request.getMontoOriginal())
            .monedaOriginal(request.getMonedaOriginal())
            .tipoCambio(request.getTipoCambio())
            .montoEnSoles(montoEnSoles)
            .costoFlete(costoFlete)
            .costoAduanas(costoAduanas)
            .costoAranceles(costoAranceles)
            .costoTransporteLocal(costoTransporte)
            .otrosCostos(otrosCostos)
            .costoTotal(costoTotal)
            .cantidadItems(request.getCantidadItems())
            .pesoTotalKg(request.getPesoTotalKg())
            .volumenTotalM3(request.getVolumenTotalM3())
            .estadoProcesamiento("procesado")
            .estadoImportacion("en_transito")
            .fechaLlegadaEstimada(request.getFechaLlegadaEstimada())
            .createdAt(LocalDateTime.now())
            .build();

        importacion = importacionRepository.save(importacion);

        // 7. PRORRATEAR COSTOS entre productos
        List<ProrrateoResponse> prorrateos = prorratearCostos(importacion, request.getItems(), costoFlete, costoAduanas, costoAranceles);

        // 8. Registrar movimientos de inventario (ENTRADA)
        for (ItemProrrateoRequest item : request.getItems()) {
            movimientoInventarioService.registrarMovimiento(
                idUsuario,
                item.getIdProducto(),
                "entrada",
                item.getCantidad(),
                "IMP-" + request.getNumeroFactura(),
                "Importación de " + request.getProveedorInternacional()
            );
        }

        if (simularError) {
            throw new RuntimeException("Rollback simulado: la importación fue revertida para demostrar @Transactional.");
        }

        return construirImportacionResponse(importacion, prorrateos);
    }

    /**
     * ALGORITMO DE PRORRATEO: Distribuye costos adicionales entre los productos
     * Basado en: valor FOB unitario (cantidad × precio unitario)
     */
    private List<ProrrateoResponse> prorratearCostos(Importacion importacion, 
                                                      List<ItemProrrateoRequest> items,
                                                      BigDecimal costoFlete,
                                                      BigDecimal costoAduanas,
                                                      BigDecimal costoAranceles) {

        List<ProrrateoResponse> prorrateos = new ArrayList<>();

        // Calcular valor FOB total (suma de precio unitario × cantidad de todos los items)
        BigDecimal valorFobTotal = items.stream()
            .map(item -> item.getPrecioUnitarioOriginal().multiply(BigDecimal.valueOf(item.getCantidad())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Para cada item, calcular su proporción y asignarle costos
        for (ItemProrrateoRequest item : items) {
            Producto producto = productoRepository.findById(item.getIdProducto()).get();

            // Valor FOB del item
            BigDecimal valorFobItem = item.getPrecioUnitarioOriginal()
                .multiply(BigDecimal.valueOf(item.getCantidad()));

            // Proporción de este item respecto al total
            BigDecimal proporcion = valorFobTotal.compareTo(BigDecimal.ZERO) > 0
                ? valorFobItem.divide(valorFobTotal, 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            // Asignar costos proporcionalmente
            BigDecimal costoFleteAsignado = costoFlete.multiply(proporcion).setScale(2, RoundingMode.HALF_UP);
            BigDecimal costoAduanasAsignado = costoAduanas.multiply(proporcion).setScale(2, RoundingMode.HALF_UP);
            BigDecimal costoArancelesAsignado = costoAranceles.multiply(proporcion).setScale(2, RoundingMode.HALF_UP);

            // Costo total adicional para este item
            BigDecimal costoAdicionalTotal = costoFleteAsignado.add(costoAduanasAsignado).add(costoArancelesAsignado);

            // Costo unitario final = precio unitario original + (costos adicionales / cantidad)
            BigDecimal costoUnitarioFinal = item.getPrecioUnitarioOriginal()
                .add(costoAdicionalTotal.divide(BigDecimal.valueOf(item.getCantidad()), 2, RoundingMode.HALF_UP));

            // Crear registro de prorrateo
            ProrrateoImportacion prorrateo = ProrrateoImportacion.builder()
                .importacion(importacion)
                .producto(producto)
                .cantidad(item.getCantidad())
                .precioUnitarioOriginal(item.getPrecioUnitarioOriginal())
                .costoFleteAsignado(costoFleteAsignado)
                .costoAduanasAsignado(costoAduanasAsignado)
                .costoArancelesAsignado(costoArancelesAsignado)
                .costoUnitarioFinal(costoUnitarioFinal)
                .createdAt(LocalDateTime.now())
                .build();

            prorrateoRepository.save(prorrateo);

            // ACTUALIZAR PRECIO DE COSTO DEL PRODUCTO con el nuevo costo calculado
            producto.setPrecioCosto(costoUnitarioFinal);
            productoRepository.save(producto);

            // Construir response
            prorrateos.add(ProrrateoResponse.builder()
                .idProrrateo(prorrateo.getIdProrrateo())
                .idProducto(producto.getIdProducto())
                .nombreProducto(producto.getNombreProducto())
                .cantidad(item.getCantidad())
                .precioUnitarioOriginal(item.getPrecioUnitarioOriginal())
                .costoFleteAsignado(costoFleteAsignado)
                .costoAduanasAsignado(costoAduanasAsignado)
                .costoArancelesAsignado(costoArancelesAsignado)
                .costoUnitarioFinal(costoUnitarioFinal)
                .build());
        }

        return prorrateos;
    }

    public ImportacionResponse obtenerImportacion(Long idImportacion) {
        Importacion importacion = importacionRepository.findById(idImportacion)
            .orElseThrow(() -> new ResourceNotFoundException("Importación no encontrada"));

        List<ProrrateoImportacion> prorrateos = prorrateoRepository.findByImportacionIdImportacion(idImportacion);
        List<ProrrateoResponse> prorrateoResponses = prorrateos.stream()
            .map(p -> ProrrateoResponse.builder()
                .idProrrateo(p.getIdProrrateo())
                .idProducto(p.getProducto().getIdProducto())
                .nombreProducto(p.getProducto().getNombreProducto())
                .cantidad(p.getCantidad())
                .precioUnitarioOriginal(p.getPrecioUnitarioOriginal())
                .costoFleteAsignado(p.getCostoFleteAsignado())
                .costoAduanasAsignado(p.getCostoAduanasAsignado())
                .costoArancelesAsignado(p.getCostoArancelesAsignado())
                .costoUnitarioFinal(p.getCostoUnitarioFinal())
                .build())
            .collect(Collectors.toList());

        return construirImportacionResponse(importacion, prorrateoResponses);
    }

    public List<ImportacionResponse> obtenerImportacionesDelUsuario(Long idUsuario) {
        List<Importacion> importaciones = importacionRepository.findByUsuarioIdUsuario(idUsuario);
        return importaciones.stream()
            .map(imp -> {
                List<ProrrateoImportacion> prorrateos = prorrateoRepository.findByImportacionIdImportacion(imp.getIdImportacion());
                List<ProrrateoResponse> prorrateoResponses = prorrateos.stream()
                    .map(p -> ProrrateoResponse.builder()
                        .idProrrateo(p.getIdProrrateo())
                        .idProducto(p.getProducto().getIdProducto())
                        .nombreProducto(p.getProducto().getNombreProducto())
                        .cantidad(p.getCantidad())
                        .precioUnitarioOriginal(p.getPrecioUnitarioOriginal())
                        .costoFleteAsignado(p.getCostoFleteAsignado())
                        .costoAduanasAsignado(p.getCostoAduanasAsignado())
                        .costoArancelesAsignado(p.getCostoArancelesAsignado())
                        .costoUnitarioFinal(p.getCostoUnitarioFinal())
                        .build())
                    .collect(Collectors.toList());
                return construirImportacionResponse(imp, prorrateoResponses);
            })
            .collect(Collectors.toList());
    }

    private ImportacionResponse construirImportacionResponse(Importacion importacion, List<ProrrateoResponse> prorrateos) {
        return ImportacionResponse.builder()
            .idImportacion(importacion.getIdImportacion())
            .idUsuario(importacion.getUsuario().getIdUsuario())
            .numeroFactura(importacion.getNumeroFactura())
            .fechaFactura(importacion.getFechaFactura())
            .proveedorInternacional(importacion.getProveedorInternacional())
            .paisOrigen(importacion.getPaisOrigen())
            .montoOriginal(importacion.getMontoOriginal())
            .monedaOriginal(importacion.getMonedaOriginal())
            .tipoCambio(importacion.getTipoCambio())
            .montoEnSoles(importacion.getMontoEnSoles())
            .costoFlete(importacion.getCostoFlete())
            .costoAduanas(importacion.getCostoAduanas())
            .costoAranceles(importacion.getCostoAranceles())
            .costoTransporteLocal(importacion.getCostoTransporteLocal())
            .otrosCostos(importacion.getOtrosCostos())
            .costoTotal(importacion.getCostoTotal())
            .cantidadItems(importacion.getCantidadItems())
            .pesoTotalKg(importacion.getPesoTotalKg())
            .volumenTotalM3(importacion.getVolumenTotalM3())
            .estadoProcesamiento(importacion.getEstadoProcesamiento())
            .estadoImportacion(importacion.getEstadoImportacion())
            .fechaLlegadaEstimada(importacion.getFechaLlegadaEstimada())
            .fechaLlegadaReal(importacion.getFechaLlegadaReal())
            .createdAt(importacion.getCreatedAt())
            .prorrateos(prorrateos)
            .build();
    }
}
