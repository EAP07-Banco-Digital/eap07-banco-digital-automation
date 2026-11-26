package co.edu.udea.eap07.certificacion.bancodigital.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/technical-features",
        glue = "co.edu.udea.eap07.certificacion.bancodigital",
        tags = "@technical-local",
        snippets = CucumberOptions.SnippetType.CAMELCASE
)
public class TechnicalRunner {
}
