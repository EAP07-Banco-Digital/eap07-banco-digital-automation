package co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion;

import java.util.Objects;

public final class SolicitudRegistro {
    private final String nombres;
    private final String apellidos;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String email;
    private final String fechaNacimiento;
    private final String telefono;
    private final String direccion;
    private final String password;
    private final String rol;

    public SolicitudRegistro(String nombres, String apellidos, String tipoDocumento,
                             String numeroDocumento, String email, String fechaNacimiento,
                             String telefono, String direccion, String password, String rol) {
        this.nombres = Objects.requireNonNull(nombres, "nombres es obligatorio");
        this.apellidos = Objects.requireNonNull(apellidos, "apellidos es obligatorio");
        this.tipoDocumento = Objects.requireNonNull(tipoDocumento, "tipoDocumento es obligatorio");
        this.numeroDocumento = Objects.requireNonNull(numeroDocumento, "numeroDocumento es obligatorio");
        this.email = Objects.requireNonNull(email, "email es obligatorio");
        this.fechaNacimiento = Objects.requireNonNull(fechaNacimiento, "fechaNacimiento es obligatorio");
        this.telefono = Objects.requireNonNull(telefono, "telefono es obligatorio");
        this.direccion = Objects.requireNonNull(direccion, "direccion es obligatorio");
        this.password = Objects.requireNonNull(password, "password es obligatorio");
        this.rol = Objects.requireNonNull(rol, "rol es obligatorio");
    }

    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getEmail() { return email; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }

    public SolicitudRegistro conNumeroDocumento(String nuevoNumeroDocumento) {
        return new SolicitudRegistro(nombres, apellidos, tipoDocumento, nuevoNumeroDocumento,
                email, fechaNacimiento, telefono, direccion, password, rol);
    }

    @Override
    public String toString() {
        return "SolicitudRegistro{" +
                "nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", tipoDocumento='" + tipoDocumento + '\'' +
                ", numeroDocumento='" + numeroDocumento + '\'' +
                ", email='" + email + '\'' +
                ", fechaNacimiento='" + fechaNacimiento + '\'' +
                ", telefono='" + telefono + '\'' +
                ", direccion='" + direccion + '\'' +
                ", password='[PROTECTED]'" +
                ", rol='" + rol + '\'' +
                '}';
    }
}
