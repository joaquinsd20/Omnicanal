package Backendcom.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearImportacionRequest {

    @NotBlank(message = "El número de factura es obligatorio")
    private String numeroFactura;

    @NotNull(message = "La fecha de factura es obligatoria")
    private LocalDate fechaFactura;

    @NotBlank(message = "El proveedor internacional es obligatorio")
    private String proveedorInternacional;

    private String paisOrigen;

    @NotNull(message = "El monto original es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto original debe ser mayor a 0")
    private BigDecimal montoOriginal;

    @NotBlank(message = "La moneda es obligatoria")
    private String monedaOriginal; // USD, EUR, etc

    @NotNull(message = "El tipo de cambio es obligatorio")
    @DecimalMin(value = "0.01", message = "El tipo de cambio debe ser mayor a 0")
    private BigDecimal tipoCambio;

    private BigDecimal costoFlete;
    private BigDecimal costoAduanas;
    private BigDecimal costoAranceles;
    private BigDecimal costoTransporteLocal;
    private BigDecimal otrosCostos;

    @NotNull(message = "La cantidad de items es obligatoria")
    @Min(value = 1, message = "Debe tener al menos 1 item")
    private Integer cantidadItems;

    private BigDecimal pesoTotalKg;
    private BigDecimal volumenTotalM3;
    private LocalDate fechaLlegadaEstimada;

    @NotEmpty(message = "Debe incluir al menos un item de prorrateo")
    private List<ItemProrrateoRequest> items;
}
