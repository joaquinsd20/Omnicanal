package Backendcom.example.Backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenta;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_canal", nullable = false)
    private Canal canal;

    @Column(nullable = false, unique = true, length = 50)
    private String numeroVenta;

    @Column(name = "fecha_venta", nullable = false, updatable = false)
    private LocalDateTime fechaVenta;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombreCliente;

    @Column(length = 100)
    private String emailCliente;

    @Column(length = 20)
    private String telefonoCliente;

    @Column(length = 11)
    private String rucCliente; // RUC del cliente para comprobantes SUNAT

    @Column(length = 100)
    private String provinciaDestino;

    @Column(length = 100)
    private String distritoDestino;

    @Column(columnDefinition = "TEXT")
    private String direccionEntrega;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El subtotal no puede ser negativo")
    @Column(nullable = false)
    private BigDecimal subtotal;

    private BigDecimal costoEnvio;

    private BigDecimal descuento;

    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El total no puede ser negativo")
    @Column(nullable = false)
    private BigDecimal total;

    @Column(length = 50)
    private String estadoVenta;

    @Column(length = 50)
    private String estadoPago;

    @Column(length = 100)
    private String agenciaEnvio;

    @Column(length = 100)
    private String numeroTracking;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private List<DetalleVenta> detalles;

    @PrePersist
    protected void onCreate() {
        fechaVenta = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (estadoVenta == null) {
            estadoVenta = "pendiente";
        }
        if (estadoPago == null) {
            estadoPago = "pendiente";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
