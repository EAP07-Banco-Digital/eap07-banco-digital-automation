package co.edu.udea.eap07.certificacion.bancodigital.stepdefinitions.autenticacion;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionRest;
import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudInicioSesion;
import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudRegistro;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion.CerrarSesion;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion.IniciarSesion;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion.RegistrarUsuario;
import co.edu.udea.eap07.certificacion.bancodigital.utils.ClavesMemoria;
import co.edu.udea.eap07.certificacion.bancodigital.utils.Endpoints;
import co.edu.udea.eap07.certificacion.bancodigital.utils.datos.DatosRegistro;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.rest.interactions.Get;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CierreSesionStepDefinitions {
    private static final String MENSAJE_EXITO = "Sesion cerrada correctamente";
    private static final String MENSAJE_TOKEN_REVOCADO = "La sesion fue cerrada. Inicia sesion de nuevo";

    private Actor cliente;
    private SolicitudRegistro registro;
    private String tokenOriginal;

    @Given("I am an authenticated customer")
    public void soyUnClienteAutenticado() {
        this.cliente = OnStage.theActorCalled("Cliente");
        this.registro = DatosRegistro.solicitudValida();

        cliente.attemptsTo(RegistrarUsuario.conDatos(registro));
        assertThat(ultimaRespuesta().statusCode(), equalTo(201));

        cliente.attemptsTo(IniciarSesion.conCredenciales(
                new SolicitudInicioSesion(registro.getEmail(), registro.getPassword())));
        assertThat(ultimaRespuesta().statusCode(), equalTo(200));

        String token = cliente.recall(ClavesMemoria.JWT);
        assertThat(token != null && !token.isBlank(), equalTo(true));
        this.tokenOriginal = token;
    }

    @And("I can access my private banking information")
    public void puedoAccederAmiInformacionBancaria() {
        cliente.attemptsTo(Get.resource(Endpoints.USUARIO_ACTUAL)
                .withNoReporting()
                .withRequest(ConfiguracionRest.conToken(ConfiguracionEntorno.desdeEntorno(), tokenOriginal)));

        Response respuesta = ultimaRespuesta();
        assertThat(respuesta.statusCode(), equalTo(200));
        assertThat(respuesta.path("email"), equalTo(registro.getEmail()));
    }

    @When("I request to sign out")
    public void solicitoCierreSesion() {
        cliente.attemptsTo(CerrarSesion.actual());
    }

    @Then("my session should be closed successfully")
    public void miSesionDebeCerrarseExitosamente() {
        assertThat(ultimaRespuesta().statusCode(), equalTo(200));
        assertThat(cliente.recall(ClavesMemoria.JWT), equalTo(null));
    }

    @And("I should see the sign-out confirmation message {string}")
    public void veoElMensajeDeCierre(String mensaje) {
        assertThat(mensaje, equalTo(MENSAJE_EXITO));
        assertThat(ultimaRespuesta().path("message"), equalTo(mensaje));
    }

    @And("I should no longer be able to access my private banking information using the closed session")
    public void noPuedoAccederConLaSesionCerrada() {
        try {
            cliente.attemptsTo(Get.resource(Endpoints.USUARIO_ACTUAL)
                    .withNoReporting()
                    .withRequest(ConfiguracionRest.conToken(ConfiguracionEntorno.desdeEntorno(), tokenOriginal)));

            Response respuesta = ultimaRespuesta();
            assertThat(respuesta.statusCode(), equalTo(401));
            assertThat(respuesta.path("errorCode"), equalTo("TOKEN_REVOKED"));
            assertThat(respuesta.path("message"), equalTo(MENSAJE_TOKEN_REVOCADO));
            assertThat(cliente.recall(ClavesMemoria.JWT), equalTo(null));
        } finally {
            tokenOriginal = null;
        }
    }

    private Response ultimaRespuesta() {
        return cliente.asksFor(LastResponse.received());
    }
}
