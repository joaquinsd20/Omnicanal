package Backendcom.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad para almacenar configuración SUNAT de cada usuario
 * Datos fiscales requeridos para emitir comprobantes electrónicos
 */
@Entity
@Table(name = "configuracion_sunat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSunat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConfiguracion;
    
    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;
    
    // Datos de la empresa
    @Column(nullable = false, length = 11)
    private String ruc; // RUC de 11 dígitos
    
    @Column(nullable = false, length = 200)
    private String razonSocial; // Nombre de la empresa
    
    @Column(length = 200)
    private String nombreComercial; // Nombre comercial (puede ser diferente)
    
    @Column(length = 100)
    private String domicilioFiscal; // Dirección fiscal
    
    // Configuración de comprobantes
    @Column(nullable = false)
    private String numeroSerieFactura = "F001"; // Serie para facturas
    
    @Column(nullable = false)
    private Integer ultimoNumeroFactura = 0; // Próximo número de factura
    
    @Column(nullable = false)
    private String numeroSerieBoleta = "B001"; // Serie para boletas
    
    @Column(nullable = false)
    private Integer ultimoNumeroBoleta = 0; // Próximo número de boleta
    
    // Certificado digital
    @Column(columnDefinition = "LONGBLOB")
    private byte[] certificadoDigital; // Archivo .pfx (contenido binario)
    
    @Column(length = 100)
    private String passwordCertificado; // Contraseña del certificado
    
    // Estados
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true; // Configuración activa/inactiva
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean pruebasMode = true; // true=ambiente de pruebas, false=producción
    
    // Auditoría
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column
    private LocalDateTime fechaActualizacion;
    
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
