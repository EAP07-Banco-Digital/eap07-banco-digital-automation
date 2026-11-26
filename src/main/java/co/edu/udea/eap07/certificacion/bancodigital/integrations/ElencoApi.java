package co.edu.udea.eap07.certificacion.bancodigital.integrations;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public final class ElencoApi extends Cast {

    public ElencoApi(ConfiguracionEntorno entorno) {
        super((Consumer<Actor>) actor -> actor.can(CallAnApi.at(
                Objects.requireNonNull(entorno, "La configuración del entorno es obligatoria").baseUrl())));
    }
}
