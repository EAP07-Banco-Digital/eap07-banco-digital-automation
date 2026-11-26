package co.edu.udea.eap07.certificacion.bancodigital.models.cuentas;

public final class SolicitudAperturaCuenta {
    private final String tipoCuenta;

    public SolicitudAperturaCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }
}
