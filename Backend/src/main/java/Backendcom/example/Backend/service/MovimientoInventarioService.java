package Backendcom.example.Backend.service;

import Backendcom.example.Backend.entity.MovimientoInventario;
import Backendcom.example.Backend.entity.Producto;
import Backendcom.example.Backend.entity.Usuario;
import Backendcom.example.Backend.exception.ResourceNotFoundException;
import Backendcom.example.Backend.exception.BusinessException;
import Backendcom.example.Backend.repository.MovimientoInventarioRepository;
import Backendcom.example.Backend.repository.ProductoRepository;
import Backendcom.example.Backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimientoInventario registrarMovimiento(Long idUsuario, Long idProducto, 
                                                      String tipoMovimiento, Integer cantidad, 
                                                      String referencia, String descripcion) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Integer stockAnterior = producto.getStockTotal();
        Integer stockPosterior = stockAnterior;

        // Aplicar cambio de stock según tipo de movimiento
        switch (tipoMovimiento.toLowerCase()) {
            case "entrada":
                stockPosterior = stockAnterior + cantidad;
                break;
            case "salida":
                if (stockAnterior < cantidad) {
                    throw new BusinessException("Stock insuficiente para " + producto.getNombreProducto() + ". Disponible: " + stockAnterior + ", solicitado: " + cantidad);
                }
                stockPosterior = stockAnterior - cantidad;
                break;
            case "ajuste":
                stockPosterior = cantidad; // Ajuste reemplaza el valor
                break;
            case "devolución":
                stockPosterior = stockAnterior + cantidad;
                break;
            default:
                throw new IllegalArgumentException("Tipo de movimiento no válido: " + tipoMovimiento);
        }

        // Crear registro del movimiento
        MovimientoInventario movimiento = MovimientoInventario.builder()
            .usuario(usuario)
            .producto(producto)
            .tipoMovimiento(tipoMovimiento)
            .cantidad(cantidad)
            .referencia(referencia)
            .descripcion(descripcion)
            .stockAnterior(stockAnterior)
            .stockPosterior(stockPosterior)
            .build();

        // Actualizar stock del producto
        producto.setStockTotal(stockPosterior);
        productoRepository.save(producto);

        return movimientoRepository.save(movimiento);
    }

    public List<MovimientoInventario> obtenerMovimientosDelProducto(Long idProducto) {
        return movimientoRepository.findByProductoIdProducto(idProducto);
    }

    public List<MovimientoInventario> obtenerMovimientosPorUsuario(Long idUsuario) {
        return movimientoRepository.findByUsuarioIdUsuario(idUsuario);
    }

    public List<MovimientoInventario> obtenerMovimientosPorTipo(String tipoMovimiento) {
        return movimientoRepository.findByTipoMovimiento(tipoMovimiento);
    }
}
