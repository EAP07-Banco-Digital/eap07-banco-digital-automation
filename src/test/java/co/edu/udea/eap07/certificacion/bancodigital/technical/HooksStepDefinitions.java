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

    @Given("the API scenario is initialized")
    public void verificarEscenarioInicializado() {
        assertNotNull(OnStage.stage());
    }

    @When("the technical actor remembers {string}")
    public void recordarValor(String valor) {
        OnStage.theActorCalled("tecnico").remember(CLAVE, valor);
    }

    @Then("the API actor has the CallAnApi ability and can recall the value")
    public void verificarActorYMemoria() {
        Actor actor = OnStage.theActorInTheSpotlight();
        assertNotNull(actor.abilityTo(CallAnApi.class));
        assertEquals("scenario-one-value", actor.recall(CLAVE));
    }

    @Then("the API actor does not inherit memory from the previous scenario")
    public void verificarMemoriaLimpia() {
        Actor actor = OnStage.theActorCalled("tecnico");
        assertNotNull(actor.abilityTo(CallAnApi.class));
        assertNull(actor.recall(CLAVE));
    }
}
