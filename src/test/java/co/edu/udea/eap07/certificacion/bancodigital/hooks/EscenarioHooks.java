package co.edu.udea.eap07.certificacion.bancodigital.hooks;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.integrations.ElencoApi;
import co.edu.udea.eap07.certificacion.bancodigital.integrations.WarmupServicio;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import net.serenitybdd.screenplay.actors.OnStage;

public class EscenarioHooks {

    @Before
    public void iniciarEscenario() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        WarmupServicio.ejecutarUnaVez(entorno);
        OnStage.setTheStage(new ElencoApi(entorno));
    }

    @After
    public void cerrarEscenario() {
        if (OnStage.theStageIsSet()) {
            OnStage.drawTheCurtain();
        }
    }
}
