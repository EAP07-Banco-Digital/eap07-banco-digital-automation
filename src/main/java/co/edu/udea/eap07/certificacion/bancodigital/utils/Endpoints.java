package co.edu.udea.eap07.certificacion.bancodigital.utils;

public final class Endpoints {

    public static final String REGISTRO = "/api/usuarios/registro";
    public static final String LOGIN = "/api/auth/login";
    public static final String DOCUMENTACION_API = "/v3/api-docs";
    public static final String LOGOUT = "/api/auth/logout";
    public static final String USUARIO_ACTUAL = "/api/usuarios/me";
    public static final String ROL_USUARIO = "/api/usuarios/{id}/rol";
    public static final String CUENTAS = "/api/v1/cuentas";
    public static final String SALDO_CUENTA = "/api/v1/cuentas/{numeroCuenta}/saldo";
    public static final String DEPOSITOS = "/api/v1/cuentas/{numeroCuenta}/depositos";
    public static final String RETIROS = "/api/v1/cuentas/{numeroCuenta}/retiros";
    public static final String TRANSACCIONES = "/api/v1/cuentas/{numeroCuenta}/transacciones";
    public static final String REPORTE_CUENTA = "/api/v1/cuentas/{numeroCuenta}/reporte";
    public static final String TRANSFERENCIAS = "/api/v1/transferencias";
    public static final String TOPES_TRANSFERENCIAS = "/api/v1/transferencias/topes";
    public static final String TOPES_ADMIN_TRANSFERENCIAS = "/api/v1/admin/transferencias/topes";
    public static final String REPORTE_ADMIN_OPERACIONES = "/api/v1/admin/reportes/operaciones";

    private Endpoints() {
    }
}
