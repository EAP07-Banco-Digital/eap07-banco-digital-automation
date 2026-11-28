package co.edu.udea.eap07.certificacion.bancodigital.tasks.perfil;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionRest;
import co.edu.udea.eap07.certificacion.bancodigital.utils.ClavesMemoria;
import co.edu.udea.eap07.certificacion.bancodigital.utils.Endpoints;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Get;

/**
 * Consulta el perfil del usuario identificado por el JWT del actor (HU4).
 * Si el actor no tiene JWT en memoria, la peticion viaja sin Authorization:
 * ese es el caso negativo de consultar el perfil sin iniciar sesion.
 */
public class ConsultarPerfil implements Task {

    // Publica y no final: Serenity instrumenta las Tasks sin parametros (igual que CerrarSesion).
    public ConsultarPerfil() {
    }

    public static ConsultarPerfil actual() {
        return new ConsultarPerfil();
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        String token = actor.recall(ClavesMemoria.JWT);
        actor.attemptsTo(Get.resource(Endpoints.USUARIO_ACTUAL)
                .withNoReporting()
                .withRequest(ConfiguracionRest.conToken(entorno, token)));
    }
}
