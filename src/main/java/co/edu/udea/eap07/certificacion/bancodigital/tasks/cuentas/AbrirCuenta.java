package co.edu.udea.eap07.certificacion.bancodigital.tasks.cuentas;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionRest;
import co.edu.udea.eap07.certificacion.bancodigital.models.cuentas.SolicitudAperturaCuenta;
import co.edu.udea.eap07.certificacion.bancodigital.utils.ClavesMemoria;
import co.edu.udea.eap07.certificacion.bancodigital.utils.Endpoints;
import io.restassured.response.Response;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

import java.util.Objects;

public final class AbrirCuenta implements Task {
    private final SolicitudAperturaCuenta solicitud;

    private AbrirCuenta(SolicitudAperturaCuenta solicitud) {
        this.solicitud = Objects.requireNonNull(solicitud, "La solicitud de apertura es obligatoria");
    }

    public static AbrirCuenta conDatos(SolicitudAperturaCuenta solicitud) {
        return new AbrirCuenta(solicitud);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        String token = actor.recall(ClavesMemoria.JWT);
        actor.attemptsTo(Post.to(Endpoints.CUENTAS)
                .withNoReporting()
                .withRequest(ConfiguracionRest.conTokenJson(entorno, token))
                .withRequest(especificacion -> especificacion.body(solicitud)));

        Response respuesta = actor.asksFor(LastResponse.received());
        if (respuesta.statusCode() == 201) {
            String numeroCuenta = respuesta.path("numeroCuenta");
            if (numeroCuenta != null && !numeroCuenta.isBlank()) {
                actor.remember(ClavesMemoria.NUMERO_CUENTA, numeroCuenta);
                Object clienteId = respuesta.path("clienteId");
                if (clienteId != null) {
                    actor.remember(ClavesMemoria.CLIENTE_ID, clienteId);
                }
            }
        }
    }
}
