package co.edu.udea.eap07.certificacion.bancodigital.stepdefinitions.perfil;

import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudInicioSesion;
import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudRegistro;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion.IniciarSesion;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion.RegistrarUsuario;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.perfil.ConsultarPerfil;
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

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class ConsultaPerfilStepDefinitions {
    private static final String MENSAJE_SIN_SESION = "Debes iniciar sesión para acceder a este recurso";
    private static final String PREFIJO_OFUSCACION = "***";
    private static final int DIGITOS_VISIBLES = 4;

    private Actor actor;
    private SolicitudRegistro registro;
    private Response respuestaPerfil;

    @Given("I am a signed-in customer with a registered profile")
    public void soyUnClienteConSesionYPerfilRegistrado() {
        this.actor = OnStage.theActorCalled("Cliente");
        this.registro = DatosRegistro.solicitudValida();

        actor.attemptsTo(RegistrarUsuario.conDatos(registro));
        assertThat(ultimaRespuesta().statusCode(), equalTo(201));

        actor.attemptsTo(IniciarSesion.conCredenciales(
                new SolicitudInicioSesion(registro.getEmail(), registro.getPassword())));
        assertThat(ultimaRespuesta().statusCode(), equalTo(200));
        String token = actor.recall(ClavesMemoria.JWT);
        assertThat(token != null && !token.isBlank(), equalTo(true));
    }

    @Given("I am a visitor without an active session")
    public void soyUnVisitanteSinSesion() {
        this.actor = OnStage.theActorCalled("Visitante");
        assertThat(actor.recall(ClavesMemoria.JWT), nullValue());
    }

    @When("I request to view my profile")
    public void solicitoConsultarMiPerfil() {
        actor.attemptsTo(ConsultarPerfil.actual());
        this.respuestaPerfil = ultimaRespuesta();
    }

    @When("I try to view a customer profile")
    public void intentoConsultarUnPerfil() {
        actor.attemptsTo(ConsultarPerfil.actual());
        this.respuestaPerfil = ultimaRespuesta();
    }

    @Then("I should see my registered personal information")
    public void veoMiInformacionPersonalRegistrada() {
        assertThat(respuestaPerfil.statusCode(), equalTo(200));
        assertThat(respuestaPerfil.path("id"), notNullValue());
        assertThat(respuestaPerfil.path("nombre"),
                equalTo(registro.getNombres() + " " + registro.getApellidos()));
        assertThat(respuestaPerfil.path("email"), equalTo(registro.getEmail()));
        assertThat(respuestaPerfil.path("fechaNacimiento"), equalTo(registro.getFechaNacimiento()));
        assertThat(respuestaPerfil.path("telefono"), equalTo(registro.getTelefono()));
        assertThat(respuestaPerfil.path("direccion"), equalTo(registro.getDireccion()));
    }

    @And("my identity document should be partially masked")
    public void miDocumentoDebeEstarOfuscado() {
        String documentoRegistrado = registro.getNumeroDocumento();
        String documentoMostrado = respuestaPerfil.path("documentoIdentidad");
        String ultimosDigitos = documentoRegistrado.substring(documentoRegistrado.length() - DIGITOS_VISIBLES);

        assertThat(documentoMostrado, notNullValue());
        assertThat(documentoMostrado.equals(documentoRegistrado), equalTo(false));
        assertThat(documentoMostrado, equalTo(PREFIJO_OFUSCACION + ultimosDigitos));
    }

    @And("my profile should not expose my password")
    public void miPerfilNoExponeLaContrasena() {
        Map<String, Object> perfil = respuestaPerfil.jsonPath().getMap("$");
        assertThat(perfil, not(hasKey("password")));
        assertThat(perfil, not(hasKey("passwordHash")));
        assertThat(respuestaPerfil.asString().contains(registro.getPassword()), equalTo(false));
    }

    @Then("my profile request should be rejected because I am not signed in")
    public void miConsultaEsRechazadaPorNoTenerSesion() {
        assertThat(respuestaPerfil.statusCode(), equalTo(401));
        assertThat(respuestaPerfil.path("errorCode"), equalTo("UNAUTHORIZED"));
    }

    @And("I should see the sign-in required message {string}")
    public void veoElMensajeDeSesionRequerida(String mensaje) {
        assertThat(mensaje, equalTo(MENSAJE_SIN_SESION));
        assertThat(respuestaPerfil.path("message"), equalTo(mensaje));
    }

    private Response ultimaRespuesta() {
        return actor.asksFor(LastResponse.received());
    }
}
