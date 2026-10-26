package Backendcom.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "importaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Importacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImportacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String numeroFactura;

    @Column(nullable = false)
    private LocalDate fechaFactura;

    @Column(nullable = false, length = 150)
    private String proveedorInternacional;

    @Column(length = 50)
    private String paisOrigen;

    @Column(nullable = false)
    private BigDecimal montoOriginal;

    @Column(length = 3)
    private String monedaOriginal;

    private BigDecimal tipoCambio;

    private BigDecimal montoEnSoles;

    private BigDecimal costoFlete;

    private BigDecimal costoAduanas;

    private BigDecimal costoAranceles;

    private BigDecimal costoTransporteLocal;

    private BigDecimal otrosCostos;

    private BigDecimal costoTotal;

    @Column(nullable = false)
    private Integer cantidadItems;

    private BigDecimal pesoTotalKg;

    private BigDecimal volumenTotalM3;

    @Column(length = 500)
    private String documentoRuta;

    @Column(length = 50)
    private String estadoProcesamiento;

    private LocalDate fechaLlegadaEstimada;

    private LocalDate fechaLlegadaReal;

    @Column(length = 50)
    private String estadoImportacion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (monedaOriginal == null) {
            monedaOriginal = "USD";
        }
        if (estadoProcesamiento == null) {
            estadoProcesamiento = "pendiente";
        }
        if (estadoImportacion == null) {
            estadoImportacion = "en_transito";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
