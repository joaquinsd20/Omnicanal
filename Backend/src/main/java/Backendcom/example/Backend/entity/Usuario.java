package Backendcom.example.Backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import Backendcom.example.Backend.enums.TipoUsuario;
import Backendcom.example.Backend.enums.EstadoUsuario;
import Backendcom.example.Backend.enums.PlanSuscripcion;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false, length = 100)
    private String nombreUsuario;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String razonSocial;

    @Column(unique = true, length = 11)
    private String ruc;

    @Column(length = 50)
    private String pais;

    @Column(length = 100)
    private String ciudad;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('EMPRENDEDOR', 'ADMINISTRADOR', 'ADMINISTRADOR_GLOBAL', 'SUPERVISOR')")
    private TipoUsuario tipoUsuario = TipoUsuario.EMPRENDEDOR;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PLAN_EMPRENDEDOR', 'PLAN_PRO')")
    private PlanSuscripcion planSuscripcion = PlanSuscripcion.PLAN_EMPRENDEDOR;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'BLOQUEADO')")
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @ManyToOne
    @JoinColumn(name = "id_usuario_creador")
    private Usuario usuarioCreador;

    @JsonIgnore
    @OneToMany(mappedBy = "usuarioCreador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Usuario> usuariosCreadosPorMi;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Producto> productos;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Canal> canales;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Venta> ventas;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Importacion> importaciones;
}
