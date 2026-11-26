package co.edu.udea.eap07.certificacion.bancodigital.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/autenticacion/hu02_customer_authentication.feature",
        glue = {
                "co.edu.udea.eap07.certificacion.bancodigital.hooks",
                "co.edu.udea.eap07.certificacion.bancodigital.stepdefinitions.autenticacion"
        },
        snippets = CucumberOptions.SnippetType.CAMELCASE
)
public class AutenticacionRunner {
}
