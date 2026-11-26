package co.edu.udea.eap07.certificacion.bancodigital.technical;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HooksStepDefinitions {

    private static final String CLAVE = "valorTecnico";

    @Given("el escenario API está inicializado")
    public void verificarEscenarioInicializado() {
        assertNotNull(OnStage.stage());
    }

    @When("el actor técnico recuerda {string}")
    public void recordarValor(String valor) {
        OnStage.theActorCalled("tecnico").remember(CLAVE, valor);
    }

    @Then("el actor API tiene CallAnApi y puede recordar el valor")
    public void verificarActorYMemoria() {
        Actor actor = OnStage.theActorInTheSpotlight();
        assertNotNull(actor.abilityTo(CallAnApi.class));
        assertEquals("valor-escenario-uno", actor.recall(CLAVE));
    }

    @Then("el actor API no hereda memoria del escenario anterior")
    public void verificarMemoriaLimpia() {
        Actor actor = OnStage.theActorCalled("tecnico");
        assertNotNull(actor.abilityTo(CallAnApi.class));
        assertNull(actor.recall(CLAVE));
    }
}
