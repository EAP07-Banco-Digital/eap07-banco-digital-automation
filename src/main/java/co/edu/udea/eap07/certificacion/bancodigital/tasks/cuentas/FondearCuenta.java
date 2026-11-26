package co.edu.udea.eap07.certificacion.bancodigital.tasks.cuentas;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionRest;
import co.edu.udea.eap07.certificacion.bancodigital.models.cuentas.SolicitudDeposito;
import co.edu.udea.eap07.certificacion.bancodigital.utils.ClavesMemoria;
import co.edu.udea.eap07.certificacion.bancodigital.utils.Endpoints;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;

import java.util.Objects;

public final class FondearCuenta implements Task {
    private final String numeroCuenta;
    private final SolicitudDeposito solicitud;

    private FondearCuenta(String numeroCuenta, SolicitudDeposito solicitud) {
        this.numeroCuenta = numeroCuenta;
        this.solicitud = Objects.requireNonNull(solicitud, "La solicitud de depósito es obligatoria");
    }

    public static FondearCuenta en(String numeroCuenta, SolicitudDeposito solicitud) {
        return new FondearCuenta(numeroCuenta, solicitud);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        String token = actor.recall(ClavesMemoria.JWT);
        actor.attemptsTo(Post.to(Endpoints.DEPOSITOS)
                .withNoReporting()
                .withRequest(ConfiguracionRest.conTokenJson(entorno, token))
                .withRequest(especificacion -> especificacion.pathParam("numeroCuenta", numeroCuenta))
                .withRequest(especificacion -> especificacion.body(solicitud)));
    }
}
