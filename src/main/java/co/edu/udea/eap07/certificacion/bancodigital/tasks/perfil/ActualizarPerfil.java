package co.edu.udea.eap07.certificacion.bancodigital.tasks.perfil;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionRest;
import co.edu.udea.eap07.certificacion.bancodigital.models.perfil.SolicitudActualizacionPerfil;
import co.edu.udea.eap07.certificacion.bancodigital.utils.ClavesMemoria;
import co.edu.udea.eap07.certificacion.bancodigital.utils.Endpoints;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Put;

import java.util.Objects;

/**
 * Actualiza los datos de contacto del usuario autenticado (HU7).
 * No valida la respuesta: las aserciones viven en las Step Definitions.
 */
public final class ActualizarPerfil implements Task {
    private final SolicitudActualizacionPerfil solicitud;

    private ActualizarPerfil(SolicitudActualizacionPerfil solicitud) {
        this.solicitud = Objects.requireNonNull(solicitud, "La solicitud de actualizacion es obligatoria");
    }

    public static ActualizarPerfil conDatos(SolicitudActualizacionPerfil solicitud) {
        return new ActualizarPerfil(solicitud);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        String token = actor.recall(ClavesMemoria.JWT);
        actor.attemptsTo(Put.to(Endpoints.USUARIO_ACTUAL)
                .withNoReporting()
                .withRequest(ConfiguracionRest.conTokenJson(entorno, token))
                .withRequest(especificacion -> especificacion.body(solicitud)));
    }
}
