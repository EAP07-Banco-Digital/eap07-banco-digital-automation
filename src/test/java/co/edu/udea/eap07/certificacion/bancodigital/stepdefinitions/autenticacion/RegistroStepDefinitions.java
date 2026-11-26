package co.edu.udea.eap07.certificacion.bancodigital.stepdefinitions.autenticacion;

import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudRegistro;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion.RegistrarUsuario;
import co.edu.udea.eap07.certificacion.bancodigital.utils.datos.DatosRegistro;
import co.edu.udea.eap07.certificacion.bancodigital.utils.DatosDePrueba;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class RegistroStepDefinitions {
    private static final String MENSAJE_EXITO = "El usuario ha sido registrado con éxito.";
    private static final String MENSAJE_EMAIL_DUPLICADO = "El correo ingresado ya está registrado.";
    private Actor visitante;
    private SolicitudRegistro solicitud;

    @Given("I am an unregistered visitor")
    public void soyUnVisitanteNoRegistrado() {
        this.visitante = OnStage.theActorCalled("Visitante");
        solicitud = DatosRegistro.solicitudValida();
    }

    @And("I provide valid personal information")
    public void proporcionoInformacionPersonalValida() { solicitud = DatosRegistro.solicitudValida(); }

    @When("I request to register")
    public void solicitoElRegistro() { visitante.attemptsTo(RegistrarUsuario.conDatos(solicitud)); }

    @Then("my customer profile should be created")
    public void miPerfilDebeSerCreado() {
        Response respuesta = ultimaRespuesta();
        assertThat(respuesta.statusCode(), equalTo(201));
        assertThat(respuesta.path("usuarioId"), notNullValue());
        assertThat(respuesta.path("email"), equalTo(solicitud.getEmail()));
    }

    @And("I should see the success message {string}")
    public void veoElMensajeDeExito(String mensaje) {
        assertThat(mensaje, equalTo(MENSAJE_EXITO));
        assertThat(ultimaRespuesta().path("message"), equalTo(mensaje));
    }

    @Given("my email address is already registered")
    public void miCorreoYaEstaRegistrado() {
        this.visitante = OnStage.theActorCalled("Visitante");
        solicitud = DatosRegistro.solicitudValida();
        visitante.attemptsTo(RegistrarUsuario.conDatos(solicitud));
        Response respuestaInicial = ultimaRespuesta();
        assertThat(respuestaInicial.statusCode(), equalTo(201));
        assertThat(respuestaInicial.path("usuarioId"), notNullValue());
        assertThat(respuestaInicial.path("email"), equalTo(solicitud.getEmail()));
    }

    @When("I request to register using the same email address")
    public void solicitoRegistroConElMismoCorreo() {
        solicitud = solicitud.conNumeroDocumento(DatosDePrueba.documentoUnico());
        visitante.attemptsTo(RegistrarUsuario.conDatos(solicitud));
    }

    @Then("my registration should be rejected")
    public void miRegistroDebeSerRechazado() {
        Response respuesta = ultimaRespuesta();
        assertThat(respuesta.statusCode(), equalTo(409));
        assertThat(respuesta.path("errorCode"), equalTo("EMAIL_ALREADY_REGISTERED"));
    }

    @And("I should see the error message {string}")
    public void veoElMensajeDeError(String mensaje) {
        assertThat(mensaje, equalTo(MENSAJE_EMAIL_DUPLICADO));
        assertThat(ultimaRespuesta().path("message"), equalTo(mensaje));
    }

    private Response ultimaRespuesta() { return visitante.asksFor(LastResponse.received()); }
}
