package co.edu.udea.eap07.certificacion.bancodigital.config;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.screenplay.rest.questions.RestQueryFunction;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.params.CoreConnectionPNames;

import java.util.Objects;

public final class ConfiguracionRest {

    private ConfiguracionRest() {
    }

    public static RestQueryFunction publica(ConfiguracionEntorno entorno) {
        return especificacion -> aplicar(especificacion, entorno, null, false);
    }

    public static RestQueryFunction publicaJson(ConfiguracionEntorno entorno) {
        return especificacion -> aplicar(especificacion, entorno, null, true);
    }

    public static RestQueryFunction conToken(ConfiguracionEntorno entorno, String token) {
        return especificacion -> aplicar(especificacion, entorno, token, false);
    }

    public static RestQueryFunction conTokenJson(ConfiguracionEntorno entorno, String token) {
        return especificacion -> aplicar(especificacion, entorno, token, true);
    }

    public static RequestSpecification aplicar(RequestSpecification especificacion,
                                               ConfiguracionEntorno entorno,
                                               String token,
                                               boolean cuerpoJson) {
        Objects.requireNonNull(especificacion, "La especificación REST es obligatoria");
        Objects.requireNonNull(entorno, "La configuración del entorno es obligatoria");

        RequestSpecification configurada = especificacion
                .config(configuracionHTTP(entorno))
                .header("Accept", "application/json");

        if (cuerpoJson) {
            configurada = configurada.contentType("application/json");
        }

        if (token != null) {
            if (token.isBlank()) {
                throw new IllegalArgumentException("El token explícito no puede estar vacío");
            }
            configurada = configurada.header("Authorization", "Bearer " + token);
        }

        return configurada;
    }

    public static RestAssuredConfig configuracionHTTP(ConfiguracionEntorno entorno) {
        Objects.requireNonNull(entorno, "La configuración del entorno es obligatoria");
        HttpClientConfig clienteHttp = HttpClientConfig.httpClientConfig()
                .setParam(CoreConnectionPNames.CONNECTION_TIMEOUT, entorno.httpConnectTimeoutMs())
                .setParam(CoreConnectionPNames.SO_TIMEOUT, entorno.httpReadTimeoutMs())
                .setParam(ConnManagerPNames.TIMEOUT, (long) entorno.httpConnectTimeoutMs());
        LogConfig logging = LogConfig.logConfig().blacklistDefaultSensitiveHeaders()
                .blacklistHeader("Authorization");
        return RestAssuredConfig.newConfig().httpClient(clienteHttp).logConfig(logging);
    }
}
