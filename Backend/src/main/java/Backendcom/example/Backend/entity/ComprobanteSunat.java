package Backendcom.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad para registrar comprobantes emitidos a la SUNAT
 */
@Entity
@Table(name = "comprobantes_sunat", indexes = {
    @Index(name = "idx_venta", columnList = "id_venta"),
    @Index(name = "idx_numero", columnList = "numero_comprobante"),
    @Index(name = "idx_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprobanteSunat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idComprobante;
    
    @ManyToOne
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;
    
    // Tipo de comprobante
    @Column(nullable = false, length = 20)
    private String tipoComprobante; // "01"=Factura, "03"=Boleta
    
    @Column(nullable = false, length = 10)
    private String serie; // Ej: "F001", "B001"
    
    @Column(nullable = false)
    private Integer numero; // Número secuencial
    
    @Column(nullable = false, length = 30)
    private String numeroComprobante; // Formato completo: "F001-000001"
    
    // Información de SUNAT
    @Column(length = 50)
    private String ticketSunat; // Ticket de SUNAT para consultar estado
    
    @Column(columnDefinition = "LONGTEXT")
    private String respuestaSunat; // Respuesta completa de SUNAT
    
    // Estados posibles: PENDIENTE, ENVIADO, ACEPTADO, RECHAZADO, OBSERVADO
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";
    
    @Column(length = 500)
    private String mensajeError; // Si fue rechazado, el motivo
    
    // XML generado
    @Column(columnDefinition = "LONGTEXT")
    private String xmlGenerado; // Comprobante en formato XML
    
    @Column(columnDefinition = "LONGTEXT")
    private String xmlFirmado; // XML firmado digitalmente
    
    // URLs
    @Column(length = 500)
    private String urlPdf; // URL o ruta del PDF del comprobante
    
    // Auditoría
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column
    private LocalDateTime fechaEnvio; // Cuándo se envió a SUNAT
    
    @Column
    private LocalDateTime fechaRespuesta; // Cuándo SUNAT respondió
    
    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
