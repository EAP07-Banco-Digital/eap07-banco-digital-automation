package co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionRest;
import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudInicioSesion;
import co.edu.udea.eap07.certificacion.bancodigital.utils.ClavesMemoria;
import co.edu.udea.eap07.certificacion.bancodigital.utils.Endpoints;
import io.restassured.response.Response;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

import java.util.Objects;

public final class IniciarSesion implements Task {
    private final SolicitudInicioSesion solicitud;

    private IniciarSesion(SolicitudInicioSesion solicitud) {
        this.solicitud = Objects.requireNonNull(solicitud, "La solicitud de inicio de sesión es obligatoria");
    }

    public static IniciarSesion conCredenciales(SolicitudInicioSesion solicitud) {
        return new IniciarSesion(solicitud);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        actor.attemptsTo(Post.to(Endpoints.LOGIN)
                .withNoReporting()
                .withRequest(ConfiguracionRest.publicaJson(entorno))
                .withRequest(especificacion -> especificacion.body(solicitud)));

        Response respuesta = actor.asksFor(LastResponse.received());
        actor.forget(ClavesMemoria.JWT);
        String token = respuesta.path("token");
        if (respuesta.statusCode() == 200 && token != null && !token.isBlank()) {
            actor.remember(ClavesMemoria.JWT, token);
        }
    }
}
