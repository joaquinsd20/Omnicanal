package Backendcom.example.Backend.dto;

import lombok.*;

/**
 * DTO para registrar/actualizar configuración SUNAT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSunatDTO {
    private Long idConfiguracion;
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String domicilioFiscal;
    private String numeroSerieFactura;
    private Integer ultimoNumeroFactura;
    private String numeroSerieBoleta;
    private Integer ultimoNumeroBoleta;
    private String passwordCertificado;
    private Boolean activo;
    private Boolean pruebasMode; // true=pruebas, false=producción
}
