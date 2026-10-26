package Backendcom.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de validación de comprobante a SUNAT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SunatValidationRequestDTO {
    
    /** RUC de quien consulta (Consultante) */
    private String rucConsultante;
    
    /** RUC del emisor del comprobante */
    private String numRuc;
    
    /** Código de comprobante: 01=Factura, 03=Boleta, 04=Liquidación, etc. */
    private String codComp;
    
    /** Serie del comprobante (Ej: F001, B001) */
    private String numeroSerie;
    
    /** Número del comprobante (secuencial) */
    private String numero;
    
    /** Fecha de emisión (formato: dd/mm/yyyy) */
    private String fechaEmision;
    
    /** Monto total del comprobante */
    private String monto;
    
    /**
     * Getter para número de comprobante formateado
     * Ejemplo: "F001-000001"
     */
    public String getNumeroComprobante() {
        return numeroSerie + "-" + numero;
    }
}
