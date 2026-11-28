package co.edu.udea.eap07.certificacion.bancodigital.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/perfil/hu07_customer_contact_update.feature",
        glue = {
                "co.edu.udea.eap07.certificacion.bancodigital.hooks",
                "co.edu.udea.eap07.certificacion.bancodigital.stepdefinitions.perfil"
        },
        snippets = CucumberOptions.SnippetType.CAMELCASE
)
public class ActualizacionPerfilRunner {
}
