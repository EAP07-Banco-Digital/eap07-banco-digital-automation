package co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion;

public final class SolicitudInicioSesion {
    private final String email;
    private final String password;

    public SolicitudInicioSesion(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "SolicitudInicioSesion{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
