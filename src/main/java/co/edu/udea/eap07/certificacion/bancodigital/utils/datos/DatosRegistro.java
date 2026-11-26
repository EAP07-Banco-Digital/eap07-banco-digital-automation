package co.edu.udea.eap07.certificacion.bancodigital.utils.datos;

import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudRegistro;
import co.edu.udea.eap07.certificacion.bancodigital.utils.DatosDePrueba;

import java.time.LocalDate;

public final class DatosRegistro {
    private DatosRegistro() {
    }

    public static SolicitudRegistro solicitudValida() {
        return new SolicitudRegistro(
                "Cliente",
                "QA",
                "CC",
                DatosDePrueba.documentoUnico(),
                DatosDePrueba.correoUnico(),
                LocalDate.now().minusYears(25).toString(),
                "3001234567",
                "Calle 1 # 2-3",
                "QaRegistro!2026",
                "CLIENTE");
    }
}
