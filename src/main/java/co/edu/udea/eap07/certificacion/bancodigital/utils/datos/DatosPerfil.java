package co.edu.udea.eap07.certificacion.bancodigital.utils.datos;

import co.edu.udea.eap07.certificacion.bancodigital.models.perfil.SolicitudActualizacionPerfil;
import co.edu.udea.eap07.certificacion.bancodigital.utils.DatosDePrueba;

public final class DatosPerfil {
    private DatosPerfil() {
    }

    /**
     * Datos de contacto distintos a los de DatosRegistro.solicitudValida(), con un
     * correo unico por escenario para no chocar con DUPLICATE_EMAIL.
     */
    public static SolicitudActualizacionPerfil contactoNuevoValido() {
        return new SolicitudActualizacionPerfil(
                "3109876543",
                DatosDePrueba.correoUnico(),
                "Carrera 45 # 10-20");
    }
}
