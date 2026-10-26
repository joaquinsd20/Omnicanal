package Backendcom.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prorrateo_importaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProrrateoImportacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProrrateo;

    @ManyToOne
    @JoinColumn(name = "id_importacion", nullable = false)
    private Importacion importacion;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    private BigDecimal precioUnitarioOriginal;

    private BigDecimal costoFleteAsignado;

    private BigDecimal costoAduanasAsignado;

    private BigDecimal costoArancelesAsignado;

    private BigDecimal costoUnitarioFinal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
