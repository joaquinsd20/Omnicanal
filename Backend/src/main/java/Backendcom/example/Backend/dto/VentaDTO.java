package Backendcom.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDTO {
    private Long idUsuario;
    private Long idCanal;
    
    // Cliente
    private String nombreCliente;
    private String emailCliente;
    private String telefonoCliente;
    private String rucCliente; // RUC para comprobantes SUNAT
    
    // Envío
    private String provinciaDestino;
    private String distritoDestino;
    private String direccionEntrega;
    
    // Montos
    private BigDecimal subtotal;
    private BigDecimal costoEnvio;
    private BigDecimal descuento;
    private BigDecimal total;
    
    // Estado
    private String estadoVenta;
    private String estadoPago;
    
    // Logística
    private String agenciaEnvio;
    
    // Detalles
    private List<DetalleVentaDTO> detalles;
}
