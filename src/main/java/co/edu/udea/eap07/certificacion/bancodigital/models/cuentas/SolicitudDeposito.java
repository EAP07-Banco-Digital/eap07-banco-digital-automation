package co.edu.udea.eap07.certificacion.bancodigital.models.cuentas;

import java.math.BigDecimal;

public final class SolicitudDeposito {
    private final BigDecimal monto;

    public SolicitudDeposito(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getMonto() {
        return monto;
    }
}
