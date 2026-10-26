package Backendcom.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoTopDTO {
    private Long idProducto;
    private String nombreProducto;
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private Integer stockActual;
    private BigDecimal margenGanancia;
    private Integer cantidadVendida;
    private BigDecimal ingresos;
}
