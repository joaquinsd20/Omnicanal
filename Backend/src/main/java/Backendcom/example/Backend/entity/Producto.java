package Backendcom.example.Backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"productos", "importaciones", "ventas", "hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombreProducto;

    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede superar los 50 caracteres")
    @Column(nullable = false, length = 50, unique = true)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 100)
    private String categoria;

    @NotNull(message = "El precio de costo es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de costo no puede ser negativo")
    @Column(nullable = false)
    private BigDecimal precioCosto;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de venta debe ser mayor que 0")
    @Column(nullable = false)
    private BigDecimal precioVenta;

    private BigDecimal margenGananciaPct;

    @NotNull(message = "El stock total es obligatorio")
    @Min(value = 0, message = "El stock total no puede ser negativo")
    @Column(nullable = false)
    private Integer stockTotal;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    private BigDecimal pesoKg;

    private BigDecimal volumenM3;

    @Column(length = 500)
    private String imagenUrl;

    @Column(length = 150)
    private String proveedor;

    @Column(length = 50)
    private String paisOrigen;

    @Column(length = 50)
    private String estado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (stockMinimo == null) {
            stockMinimo = 5;
        }
        // Calcular margen si no está seteado
        calcularMargen();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calcularMargen();
    }

    /**
     * Calcula el margen de ganancia como porcentaje.
     * Si margenGananciaPct es 0 o null, lo calcula desde los precios.
     * Si precioVenta no es suficiente, usa margenGananciaPct para calcularlo.
     */
    private void calcularMargen() {
        if (precioCosto == null || precioCosto.signum() == 0) {
            return;
        }

        if (margenGananciaPct != null && margenGananciaPct.signum() > 0) {
            // Si tiene margen deseado, calcular precioVenta
            BigDecimal factor = margenGananciaPct.divide(new BigDecimal(100)).add(BigDecimal.ONE);
            this.precioVenta = precioCosto.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP);
        } else if (precioVenta != null && precioVenta.signum() > 0) {
            // Si tiene precioVenta, calcular margen
            BigDecimal ganancia = precioVenta.subtract(precioCosto);
            this.margenGananciaPct = ganancia.divide(precioCosto, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }
    }

    /**
     * Establece el margen deseado y recalcula el precio de venta
     */
    public void setMargenDeseado(BigDecimal margenPct) {
        this.margenGananciaPct = margenPct;
        calcularMargen();
    }
}
