package Backendcom.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportacionResponse {

    private Long idImportacion;
    private Long idUsuario;
    private String numeroFactura;
    private LocalDate fechaFactura;
    private String proveedorInternacional;
    private String paisOrigen;
    private BigDecimal montoOriginal;
    private String monedaOriginal;
    private BigDecimal tipoCambio;
    private BigDecimal montoEnSoles;
    private BigDecimal costoFlete;
    private BigDecimal costoAduanas;
    private BigDecimal costoAranceles;
    private BigDecimal costoTransporteLocal;
    private BigDecimal otrosCostos;
    private BigDecimal costoTotal;
    private Integer cantidadItems;
    private BigDecimal pesoTotalKg;
    private BigDecimal volumenTotalM3;
    private String estadoProcesamiento;
    private String estadoImportacion;
    private LocalDate fechaLlegadaEstimada;
    private LocalDate fechaLlegadaReal;
    private LocalDateTime createdAt;
    private List<ProrrateoResponse> prorrateos;
}
