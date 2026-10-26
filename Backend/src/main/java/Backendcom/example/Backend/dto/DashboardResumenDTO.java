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
public class DashboardResumenDTO {
    private BigDecimal totalIngresos;
    private Integer totalVentas;
    private BigDecimal ticketPromedio;
    private BigDecimal totalCostos;
    private BigDecimal costoImportaciones;
    private BigDecimal costoEnvios;
    private BigDecimal gananciaNeta;
    private BigDecimal margenNetoPct;
    private BigDecimal valorInventario;
    private String mes;
}
