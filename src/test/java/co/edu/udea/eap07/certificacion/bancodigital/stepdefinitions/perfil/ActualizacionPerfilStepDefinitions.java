package co.edu.udea.eap07.certificacion.bancodigital.stepdefinitions.perfil;

import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudInicioSesion;
import co.edu.udea.eap07.certificacion.bancodigital.models.autenticacion.SolicitudRegistro;
import co.edu.udea.eap07.certificacion.bancodigital.models.perfil.SolicitudActualizacionPerfil;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion.IniciarSesion;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.autenticacion.RegistrarUsuario;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.perfil.ActualizarPerfil;
import co.edu.udea.eap07.certificacion.bancodigital.tasks.perfil.ConsultarPerfil;
import co.edu.udea.eap07.certificacion.bancodigital.utils.ClavesMemoria;
import co.edu.udea.eap07.certificacion.bancodigital.utils.datos.DatosPerfil;
import co.edu.udea.eap07.certificacion.bancodigital.utils.datos.DatosRegistro;
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
import static org.hamcrest.Matchers.not;

public class ActualizacionPerfilStepDefinitions {
    private Actor cliente;
    private SolicitudRegistro registro;
    private SolicitudActualizacionPerfil nuevoContacto;
    private String documentoMostradoAntes;

    @Given("I am a signed-in customer who wants to update my contact information")
    public void soyUnClienteQueQuiereActualizarSuContacto() {
        this.cliente = OnStage.theActorCalled("Cliente");
        this.registro = DatosRegistro.solicitudValida();

        cliente.attemptsTo(RegistrarUsuario.conDatos(registro));
        assertThat(ultimaRespuesta().statusCode(), equalTo(201));

        cliente.attemptsTo(IniciarSesion.conCredenciales(
                new SolicitudInicioSesion(registro.getEmail(), registro.getPassword())));
        assertThat(ultimaRespuesta().statusCode(), equalTo(200));
        String token = cliente.recall(ClavesMemoria.JWT);
        assertThat(token != null && !token.isBlank(), equalTo(true));

        // Se guarda el documento ofuscado para comprobar despues que no cambia.
        cliente.attemptsTo(ConsultarPerfil.actual());
        Response perfilInicial = ultimaRespuesta();
        assertThat(perfilInicial.statusCode(), equalTo(200));
        this.documentoMostradoAntes = perfilInicial.path("documentoIdentidad");
    }

    @And("I provide new valid contact information")
    public void proporcionoNuevosDatosDeContacto() {
        this.nuevoContacto = DatosPerfil.contactoNuevoValido();
        assertThat(nuevoContacto.getEmail(), not(equalTo(registro.getEmail())));
        assertThat(nuevoContacto.getTelefono(), not(equalTo(registro.getTelefono())));
        assertThat(nuevoContacto.getDireccion(), not(equalTo(registro.getDireccion())));
    }

    @When("I request to update my contact information")
    public void solicitoActualizarMiContacto() {
        cliente.attemptsTo(ActualizarPerfil.conDatos(nuevoContacto));
    }

    @Then("my contact information should be updated")
    public void miContactoDebeQuedarActualizado() {
        Response respuesta = ultimaRespuesta();
        assertThat(respuesta.statusCode(), equalTo(200));
        validarContactoNuevo(respuesta);
    }

    @And("my profile should keep the new contact information when I view it again")
    public void miPerfilConservaElNuevoContacto() {
        cliente.attemptsTo(ConsultarPerfil.actual());
        Response respuesta = ultimaRespuesta();
        assertThat(respuesta.statusCode(), equalTo(200));
        validarContactoNuevo(respuesta);
        assertThat(respuesta.path("nombre"),
                equalTo(registro.getNombres() + " " + registro.getApellidos()));
        assertThat(respuesta.path("documentoIdentidad"), equalTo(documentoMostradoAntes));
    }

    private void validarContactoNuevo(Response respuesta) {
        assertThat(respuesta.path("telefono"), equalTo(nuevoContacto.getTelefono()));
        assertThat(respuesta.path("email"), equalTo(nuevoContacto.getEmail()));
        assertThat(respuesta.path("direccion"), equalTo(nuevoContacto.getDireccion()));
    }

    private Response ultimaRespuesta() {
        return cliente.asksFor(LastResponse.received());
    }
}
