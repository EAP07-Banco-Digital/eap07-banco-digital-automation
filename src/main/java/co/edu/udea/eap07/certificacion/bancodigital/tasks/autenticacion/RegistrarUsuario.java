package co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionRest;
import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudRegistro;
import co.edu.udea.eap07.certificacion.bancodigital.utils.Endpoints;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;

import java.util.Objects;

public final class RegistrarUsuario implements Task {
    private final SolicitudRegistro solicitud;

    private RegistrarUsuario(SolicitudRegistro solicitud) {
        this.solicitud = Objects.requireNonNull(solicitud, "La solicitud de registro es obligatoria");
    }

    public static RegistrarUsuario conDatos(SolicitudRegistro solicitud) {
        return new RegistrarUsuario(solicitud);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        actor.attemptsTo(Post.to(Endpoints.REGISTRO)
                .withNoReporting()
                .withRequest(ConfiguracionRest.publicaJson(entorno))
                .withRequest(especificacion -> especificacion.body(solicitud)));
    }
}
