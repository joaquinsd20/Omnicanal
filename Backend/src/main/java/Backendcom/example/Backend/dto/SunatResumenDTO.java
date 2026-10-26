package Backendcom.example.Backend.dto;

import lombok.*;

/**
 * DTO para resumen de estado de comprobantes SUNAT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SunatResumenDTO {
    private long aceptados;
    private long rechazados;
    private int pendientes;
}
