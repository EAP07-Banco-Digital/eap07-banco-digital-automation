package co.edu.udea.eap07.certificacion.bancodigital.models.perfil;

import java.util.Objects;

/**
 * Cuerpo de PUT /api/usuarios/me (HU7). El backend exige los tres datos de contacto.
 * No incluye el documento de identidad: si viajara con cualquier valor, el backend
 * rechazaria la peticion con FIELD_NOT_EDITABLE.
 */
public final class SolicitudActualizacionPerfil {
    private final String telefono;
    private final String email;
    private final String direccion;

    public SolicitudActualizacionPerfil(String telefono, String email, String direccion) {
        this.telefono = Objects.requireNonNull(telefono, "telefono es obligatorio");
        this.email = Objects.requireNonNull(email, "email es obligatorio");
        this.direccion = Objects.requireNonNull(direccion, "direccion es obligatorio");
    }

    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public String getDireccion() { return direccion; }

    @Override
    public String toString() {
        return "SolicitudActualizacionPerfil{" +
                "telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", direccion='" + direccion + '\'' +
                '}';
    }
}
