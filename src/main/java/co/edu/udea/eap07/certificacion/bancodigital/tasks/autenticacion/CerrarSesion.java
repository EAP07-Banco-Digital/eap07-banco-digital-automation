package co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionRest;
import co.edu.udea.eap07.certificacion.bancodigital.utils.ClavesMemoria;
import co.edu.udea.eap07.certificacion.bancodigital.utils.Endpoints;
import io.restassured.response.Response;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

public class CerrarSesion implements Task {

    public CerrarSesion() {
    }

    public static CerrarSesion actual() {
        return new CerrarSesion();
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        String token = actor.recall(ClavesMemoria.JWT);
        actor.attemptsTo(Post.to(Endpoints.LOGOUT)
                .withNoReporting()
                .withRequest(ConfiguracionRest.conToken(entorno, token)));

        Response respuesta = actor.asksFor(LastResponse.received());
        if (respuesta.statusCode() == 200) {
            actor.forget(ClavesMemoria.JWT);
        }
    }
}
