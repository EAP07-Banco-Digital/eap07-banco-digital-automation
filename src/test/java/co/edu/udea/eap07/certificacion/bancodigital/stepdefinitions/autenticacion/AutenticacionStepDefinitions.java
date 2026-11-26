package co.edu.udea.eap07.certificacion.bancodigital.stepdefinitions.autenticacion;

import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudInicioSesion;
import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudRegistro;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion.IniciarSesion;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion.RegistrarUsuario;
import co.edu.udea.eap07.certificacion.bancodigital.utils.ClavesMemoria;
import co.edu.udea.eap07.certificacion.bancodigital.utils.datos.DatosRegistro;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class AutenticacionStepDefinitions {
    private static final Pattern EXP_CLAIM = Pattern.compile("\\\"exp\\\"\\s*:\\s*(\\d+)");
    private Actor cliente;
    private SolicitudRegistro registro;
    private SolicitudInicioSesion credenciales;

    @Given("I am a registered customer with an unlocked account")
    public void soyUnClienteRegistradoConCuentaDesbloqueada() {
        this.cliente = OnStage.theActorCalled("Cliente");
        this.registro = DatosRegistro.solicitudValida();
        cliente.attemptsTo(RegistrarUsuario.conDatos(registro));
        assertThat(ultimaRespuesta().statusCode(), equalTo(201));
    }

    @And("I provide valid credentials")
    public void proporcionoCredencialesValidas() {
        this.credenciales = new SolicitudInicioSesion(registro.getEmail(), registro.getPassword());
    }

    @When("I request to sign in")
    public void solicitoInicioSesion() {
        cliente.attemptsTo(IniciarSesion.conCredenciales(credenciales));
    }

    @Then("my credentials should be accepted")
    public void misCredencialesDebenSerAceptadas() {
        Response respuesta = ultimaRespuesta();
        assertThat(respuesta.statusCode(), equalTo(200));
        assertThat(cliente.recall(ClavesMemoria.JWT), notNullValue());
        assertThat(((String) cliente.recall(ClavesMemoria.JWT)).isBlank(), equalTo(false));
        assertThat(respuesta.path("tipo"), equalTo("Bearer"));
    }

    @And("I should receive an access token with an expiration date")
    public void reciboTokenConFechaExpiracion() {
        String token = cliente.recall(ClavesMemoria.JWT);
        Matcher matcher = EXP_CLAIM.matcher(new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8));
        assertThat(matcher.find(), equalTo(true));
        assertThat(Long.parseLong(matcher.group(1)), greaterThan(Instant.now().getEpochSecond()));
    }

    @And("I should be identified with my customer role")
    public void soyIdentificadoConRolCliente() {
        assertThat(ultimaRespuesta().path("rol"), equalTo("CLIENTE"));
    }

    @And("I have made four consecutive failed sign-in attempts")
    public void realizoCuatroIntentosFallidos() {
        SolicitudInicioSesion incorrectas = new SolicitudInicioSesion(registro.getEmail(), registro.getPassword() + "-incorrecta");
        for (int intento = 0; intento < 4; intento++) {
            cliente.attemptsTo(IniciarSesion.conCredenciales(incorrectas));
            Response respuesta = ultimaRespuesta();
            assertThat(respuesta.statusCode(), equalTo(401));
            assertThat(respuesta.path("errorCode"), equalTo("INVALID_CREDENTIALS"));
            assertThat(cliente.recall(ClavesMemoria.JWT), nullValue());
        }
        this.credenciales = incorrectas;
    }

    @When("I request to sign in again with an incorrect password")
    public void solicitoInicioSesionConClaveIncorrecta() {
        cliente.attemptsTo(IniciarSesion.conCredenciales(credenciales));
    }

    @Then("my account should be temporarily locked")
    public void miCuentaDebeBloquearseTemporalmente() {
        Response respuesta = ultimaRespuesta();
        assertThat(respuesta.statusCode(), equalTo(403));
        assertThat(respuesta.path("errorCode"), equalTo("ACCOUNT_LOCKED"));
        assertThat(cliente.recall(ClavesMemoria.JWT), nullValue());
    }

    @And("I should see the warning message {string}")
    public void veoMensajeDeAdvertencia(String mensaje) {
        assertThat(ultimaRespuesta().path("message"), equalTo(mensaje));
    }

    @And("I should not be able to sign in even with valid credentials")
    public void noPuedoIniciarSesionConCredencialesValidas() {
        cliente.attemptsTo(IniciarSesion.conCredenciales(
                new SolicitudInicioSesion(registro.getEmail(), registro.getPassword())));
        Response respuesta = ultimaRespuesta();
        assertThat(respuesta.statusCode(), equalTo(403));
        assertThat(respuesta.path("errorCode"), equalTo("ACCOUNT_LOCKED"));
        assertThat(respuesta.path("message"), equalTo("Cuenta temporalmente bloqueada"));
        assertThat(cliente.recall(ClavesMemoria.JWT), nullValue());
    }

    private Response ultimaRespuesta() {
        return cliente.asksFor(LastResponse.received());
    }
}
