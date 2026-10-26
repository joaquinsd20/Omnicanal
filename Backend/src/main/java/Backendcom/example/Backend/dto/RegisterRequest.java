package Backendcom.example.Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String telefono;

    @Size(max = 150, message = "La razón social no puede superar los 150 caracteres")
    private String razonSocial;

    @Pattern(regexp = "^$|^[0-9]{11}$", message = "El RUC debe tener exactamente 11 dígitos")
    private String ruc;

    private String ciudad;

    @Size(max = 50, message = "El tipo de usuario no puede superar los 50 caracteres")
    private String tipoUsuario; // NUEVO: Permitir seleccionar tipo de usuario (emprendedor, administrador_global, etc)
}
