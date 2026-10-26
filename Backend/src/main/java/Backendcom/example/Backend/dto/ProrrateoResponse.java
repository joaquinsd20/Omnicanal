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
public class ProrrateoResponse {

    private Long idProrrateo;
    private Long idProducto;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitarioOriginal;
    private BigDecimal costoFleteAsignado;
    private BigDecimal costoAduanasAsignado;
    private BigDecimal costoArancelesAsignado;
    private BigDecimal costoUnitarioFinal;
}
