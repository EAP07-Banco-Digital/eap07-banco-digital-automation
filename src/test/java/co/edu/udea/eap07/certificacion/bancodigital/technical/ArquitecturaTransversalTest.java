package co.edu.udea.eap07.certificacion.bancodigital.technical;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionRest;
import co.edu.udea.eap07.certificacion.bancodigital.integrations.ElencoApi;
import co.edu.udea.eap07.certificacion.bancodigital.integrations.WarmupServicio;
import co.edu.udea.eap07.certificacion.bancodigital.utils.ClavesMemoria;
import co.edu.udea.eap07.certificacion.bancodigital.utils.DatosDePrueba;
import io.restassured.config.RestAssuredConfig;
import io.restassured.RestAssured;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.screenplay.rest.interactions.Get;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.serenitybdd.screenplay.rest.interactions.Put;
import net.serenitybdd.screenplay.rest.questions.LastResponse;
import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static net.serenitybdd.screenplay.rest.questions.ResponseConsequence.seeThatResponse;

@RunWith(SerenityRunner.class)
public class ArquitecturaTransversalTest {

    private static ServidorLocal servidor;

    @BeforeClass
    public static void iniciarServidorLocal() throws IOException {
        servidor = new ServidorLocal();
        servidor.iniciar();
    }

    @AfterClass
    public static void detenerServidorLocal() throws IOException {
        servidor.detener();
    }

    @Before
    public void limpiarStage() {
        if (OnStage.theStageIsSet()) {
            OnStage.drawTheCurtain();
        }
    }

    @Test
    public void losActoresTienenHabilidadesYMemoriaIndependientes() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        OnStage.setTheStage(new ElencoApi(entorno));

        Actor clienteA = OnStage.theActorCalled("clienteA");
        Actor clienteB = OnStage.theActorCalled("clienteB");
        clienteA.remember(ClavesMemoria.JWT, "token-a");

        assertNotSame(clienteA.abilityTo(CallAnApi.class), clienteB.abilityTo(CallAnApi.class));
        assertEquals("token-a", clienteA.recall(ClavesMemoria.JWT));
        assertNull(clienteB.recall(ClavesMemoria.JWT));

        OnStage.drawTheCurtain();
        OnStage.setTheStage(new ElencoApi(entorno));
        assertNull(OnStage.theActorCalled("clienteA").recall(ClavesMemoria.JWT));
        OnStage.drawTheCurtain();
    }

    @Test
    public void ejecutaGetPostPutConRutasRelativasYAutenticacionExplicita() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        Actor actor = Actor.named("cliente").whoCan(CallAnApi.at(servidor.baseUrl()));

        actor.attemptsTo(Get.resource("/get")
                .withRequest(ConfiguracionRest.publica(entorno)));
        actor.attemptsTo(Post.to("/post")
                .withRequest(ConfiguracionRest.publicaJson(entorno))
                .withRequest(especificacion -> especificacion.body("{\"dato\":\"prueba\"}")));
        actor.attemptsTo(Put.to("/put")
                .withRequest(ConfiguracionRest.publicaJson(entorno))
                .withRequest(especificacion -> especificacion.body("{\"dato\":\"prueba\"}")));

        actor.remember(ClavesMemoria.JWT, "token-del-actor");
        actor.attemptsTo(Get.resource("/actor")
                .withRequest(ConfiguracionRest.conToken(entorno, actor.recall(ClavesMemoria.JWT))));
        actor.attemptsTo(Get.resource("/explicito")
                .withRequest(ConfiguracionRest.conToken(entorno, "token-explicito")));

        assertTrue(servidor.recibio("GET /get"));
        assertTrue(servidor.recibio("POST /post"));
        assertTrue(servidor.recibio("PUT /put"));
        assertTrue(servidor.recibio("Authorization: Bearer token-del-actor"));
        assertTrue(servidor.recibio("Authorization: Bearer token-explicito"));
        assertEquals(200, actor.asksFor(LastResponse.received()).statusCode());
    }

    @Test
    public void operacionSensiblePuedeConsultarRespuestaSinReportarPayload() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        Actor actor = Actor.named("autenticacion").whoCan(CallAnApi.at(servidor.baseUrl()));

        actor.attemptsTo(Post.to("/api/auth/login")
                .withNoReporting()
                .withRequest(ConfiguracionRest.publicaJson(entorno))
                .withRequest(especificacion -> especificacion.body(
                        "{\"email\":\"login.fake@example.invalid\",\"password\":\"Password-Fake-9!\"}")));

        actor.should(seeThatResponse(response -> response.statusCode(200)));
        assertEquals("JWT-FICTICIO_DISTINTIVO", actor.asksFor(LastResponse.received()).path("token"));
    }

    @Test(expected = AssertionError.class)
    public void assertionRESTFallidaSePropagaSinPayloadSensible() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        Actor actor = Actor.named("autenticacion").whoCan(CallAnApi.at(servidor.baseUrl()));
        actor.attemptsTo(Post.to("/api/auth/login")
                .withNoReporting()
                .withRequest(ConfiguracionRest.publicaJson(entorno))
                .withRequest(especificacion -> especificacion.body(
                        "{\"email\":\"fallo.fake@example.invalid\",\"password\":\"Password-Fake-7!\"}")));
        actor.should(seeThatResponse(response -> response.statusCode(401)));
    }

    @Test
    public void lastResponsePerteneceAlContextoRESTDelHiloYNoAlActor() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        Actor actorA = Actor.named("actorA").whoCan(CallAnApi.at(servidor.baseUrl()));
        Actor actorB = Actor.named("actorB").whoCan(CallAnApi.at(servidor.baseUrl()));

        actorA.attemptsTo(Get.resource("/actor-a")
                .withRequest(ConfiguracionRest.publica(entorno)));
        actorB.attemptsTo(Get.resource("/actor-b")
                .withRequest(ConfiguracionRest.publica(entorno)));

        assertEquals("/actor-b", actorA.asksFor(LastResponse.received()).path("endpoint"));
        assertEquals("/actor-b", actorB.asksFor(LastResponse.received()).path("endpoint"));
    }

    @Test
    public void aplicaTimeoutsPorPeticionYListaNegraAuthorization() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        RestAssuredConfig configuracion = ConfiguracionRest.configuracionHTTP(entorno);

        assertEquals(entorno.httpConnectTimeoutMs(), configuracion.getHttpClientConfig()
                .params().get("http.connection.timeout"));
        assertEquals(entorno.httpReadTimeoutMs(), configuracion.getHttpClientConfig()
                .params().get("http.socket.timeout"));
        assertTrue(configuracion.getLogConfig().blacklistedHeaders().stream()
                .anyMatch(header -> header.equalsIgnoreCase("Authorization")));

        assumeTrue("La prueba de demora requiere timeout reducido", entorno.httpReadTimeoutMs() <= 1000);
        boolean timeout = false;
        try {
            RestAssured.given()
                    .config(configuracion)
                    .get(servidor.baseUrl() + "/delay");
        } catch (Throwable ignored) {
            timeout = true;
        }
        assertTrue(timeout);
        assertEquals(1, servidor.contador("/delay"));
    }

    @Test
    public void postAnteCierreDeConexionNoSeReintentaImplicitamente() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        try {
            RestAssured.given()
                    .config(ConfiguracionRest.configuracionHTTP(entorno))
                    .contentType("application/json")
                    .body("{\"operacion\":\"no-reintentar\"}")
                    .post(servidor.baseUrl() + "/post-fail");
        } catch (Exception esperado) {
            // El servidor local cierra la conexión deliberadamente.
        }
        assertEquals(1, servidor.contador("/post-fail"));
    }

    @Test
    public void warmupLocalReintentaSoloErroresTransitorios() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        assumeTrue("La prueba de warmup requiere tres intentos configurados",
                entorno.warmupMaxAttempts() >= 3 && entorno.warmupRetryDelayMs() <= 1000);

        WarmupServicio.ejecutarEn(entorno, servidor.baseUrl(), "/warmup");

        assertEquals(3, servidor.contador("/warmup"));
    }

    @Test
    public void warmupAgotaErroresCincoXXSinReintentarIndefinidamente() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        boolean fallo = false;
        try {
            WarmupServicio.ejecutarEn(entorno, servidor.baseUrl(), "/permanente");
        } catch (IllegalStateException esperado) {
            fallo = true;
        }
        assertTrue(fallo);
        assertEquals(entorno.warmupMaxAttempts(), servidor.contador("/permanente"));
    }

    @Test
    public void warmupCuatroXXFallaSinReintento() {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        boolean fallo = false;
        try {
            WarmupServicio.ejecutarEn(entorno, servidor.baseUrl(), "/bad");
        } catch (IllegalStateException esperado) {
            fallo = true;
        }
        assertTrue(fallo);
        assertEquals(1, servidor.contador("/bad"));
    }

    @Test
    public void warmupUnaVezBloqueaElSegundoHiloHastaElExito() throws Exception {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        servidor.prepararWarmupLento();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Void> primero = executor.submit(() -> {
                WarmupServicio.ejecutarUnaVezEn(entorno, servidor.baseUrl());
                return null;
            });
            servidor.esperarInicioWarmup();
            Future<Void> segundo = executor.submit(() -> {
                WarmupServicio.ejecutarUnaVezEn(entorno, servidor.baseUrl());
                return null;
            });
            Thread.sleep(30);
            assertTrue(!segundo.isDone());
            primero.get();
            segundo.get();
            assertEquals(3, servidor.contador("/v3/api-docs"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void warmupEsIndependienteParaDosDestinosDistintos() throws Exception {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        ServidorLocal segundoServidor = new ServidorLocal();
        segundoServidor.iniciar();
        try {
            WarmupServicio.ejecutarUnaVezEn(entorno, servidor.baseUrl());
            WarmupServicio.ejecutarUnaVezEn(entorno, segundoServidor.baseUrl());

            assertEquals(3, servidor.contador("/v3/api-docs"));
            assertEquals(3, segundoServidor.contador("/v3/api-docs"));
        } finally {
            segundoServidor.detener();
        }
    }

    @Test
    public void warmupNoDuplicaUnDestinoYaCalentado() throws Exception {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        ServidorLocal destino = new ServidorLocal();
        destino.iniciar();
        try {
            WarmupServicio.ejecutarUnaVezEn(entorno, destino.baseUrl());
            int solicitudesTrasPrimerCalentamiento = destino.contador("/v3/api-docs");
            WarmupServicio.ejecutarUnaVezEn(entorno, destino.baseUrl() + "/");

            assertEquals(solicitudesTrasPrimerCalentamiento, destino.contador("/v3/api-docs"));
        } finally {
            destino.detener();
        }
    }

    @Test
    public void warmupPermiteReintentarElMismoDestinoTrasUnFallo() throws Exception {
        ConfiguracionEntorno entorno = ConfiguracionEntorno.desdeEntorno();
        ServidorLocal destino = new ServidorLocal();
        destino.iniciar();
        try {
            destino.prepararFalloInicialWarmup(entorno.warmupMaxAttempts());
            boolean fallo = false;
            try {
                WarmupServicio.ejecutarUnaVezEn(entorno, destino.baseUrl());
            } catch (IllegalStateException esperado) {
                fallo = true;
            }
            assertTrue(fallo);

            WarmupServicio.ejecutarUnaVezEn(entorno, destino.baseUrl());
            assertEquals(entorno.warmupMaxAttempts() + 1, destino.contador("/v3/api-docs"));
        } finally {
            destino.detener();
        }
    }

    @Test
    public void datosSinteticosTienenDiversidadEnEjecucionConcurrente() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            Set<String> correos = Collections.synchronizedSet(new HashSet<>());
            Set<String> documentos = Collections.synchronizedSet(new HashSet<>());
            Set<Callable<Void>> tareas = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                tareas.add(() -> {
                    correos.add(DatosDePrueba.correoUnico());
                    documentos.add(DatosDePrueba.documentoUnico());
                    return null;
                });
            }
            for (Future<Void> resultado : executor.invokeAll(tareas)) {
                resultado.get();
            }
            assertEquals(100, correos.size());
            assertEquals(100, documentos.size());
            assertTrue(correos.stream().allMatch(correo -> correo.endsWith("@example.com")
                    && correo.equals(correo.toLowerCase()) && correo.length() <= 255));
            assertTrue(documentos.stream().allMatch(documento -> documento.matches("\\d+")
                    && documento.length() <= 255));
        } finally {
            executor.shutdownNow();
        }
    }

    private static final class ServidorLocal {
        private final ServerSocket socket;
        private final List<String> solicitudes = Collections.synchronizedList(new java.util.ArrayList<>());
        private final CountDownLatch inicioWarmup = new CountDownLatch(1);
        private volatile boolean activo;
        private volatile boolean warmupLento;
        private volatile int fallosInicialesWarmup;
        private volatile boolean recuperacionDespuesDeFallo;
        private Thread worker;

        private ServidorLocal() throws IOException {
            socket = new ServerSocket(0);
        }

        private void iniciar() {
            activo = true;
            worker = new Thread(this::atender, "servidor-http-tecnico");
            worker.setDaemon(true);
            worker.start();
        }

        private String baseUrl() {
            return "http://127.0.0.1:" + socket.getLocalPort();
        }

        private int contador(String ruta) {
            synchronized (solicitudes) {
                return (int) solicitudes.stream().filter(solicitud -> solicitud.contains(" " + ruta + " ")).count();
            }
        }

        private boolean recibio(String texto) {
            synchronized (solicitudes) {
                return solicitudes.stream().anyMatch(solicitud -> solicitud.contains(texto));
            }
        }

        private void atender() {
            while (activo) {
                try (Socket cliente = socket.accept()) {
                    atender(cliente);
                } catch (IOException ignored) {
                    // El cierre controlado del socket y la cancelación por timeout son esperados.
                }
            }
        }

        private void atender(Socket cliente) throws IOException {
            cliente.setSoTimeout(5000);
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
            String primeraLinea = entrada.readLine();
            if (primeraLinea == null) {
                return;
            }
            StringBuilder solicitud = new StringBuilder(primeraLinea).append('\n');
            String linea;
            while ((linea = entrada.readLine()) != null && !linea.isEmpty()) {
                solicitud.append(linea).append('\n');
            }
            solicitudes.add(solicitud.toString());

            if (primeraLinea.contains(" /delay ")) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }

            if (primeraLinea.contains(" /v3/api-docs ")) {
                inicioWarmup.countDown();
                if (warmupLento) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (primeraLinea.contains(" /post-fail ")) {
                cliente.close();
                return;
            }

            int status = 200;
            if (primeraLinea.contains(" /warmup ") && contador("/warmup") < 3) {
                status = 503;
            }
            if (primeraLinea.contains(" /v3/api-docs ")) {
                if (recuperacionDespuesDeFallo) {
                    if (fallosInicialesWarmup > 0) {
                        fallosInicialesWarmup--;
                        status = 503;
                    }
                } else if (contador("/v3/api-docs") < 3) {
                    status = 503;
                }
            }
            if (primeraLinea.contains(" /permanente ")) {
                status = 503;
            }
            if (primeraLinea.contains(" /bad ")) {
                status = 401;
            }
            String contenido = "{}";
            if (primeraLinea.contains(" /api/auth/login ")) {
                contenido = "{\"token\":\"JWT-FICTICIO_DISTINTIVO\"}";
            } else if (primeraLinea.contains(" /actor-a ")) {
                contenido = "{\"endpoint\":\"/actor-a\"}";
            } else if (primeraLinea.contains(" /actor-b ")) {
                contenido = "{\"endpoint\":\"/actor-b\"}";
            }
            byte[] cuerpo = contenido.getBytes(StandardCharsets.UTF_8);
            String respuesta = "HTTP/1.1 " + status + " " + (status == 200 ? "OK" : "Service Unavailable")
                    + "\r\nContent-Type: application/json\r\nContent-Length: " + cuerpo.length
                    + "\r\nConnection: close\r\n\r\n";
            OutputStream salida = cliente.getOutputStream();
            salida.write(respuesta.getBytes(StandardCharsets.UTF_8));
            salida.write(cuerpo);
            salida.flush();
        }

        private void detener() throws IOException {
            activo = false;
            socket.close();
        }

        private void prepararWarmupLento() {
            warmupLento = true;
        }

        private void prepararFalloInicialWarmup(int intentos) {
            fallosInicialesWarmup = intentos;
            recuperacionDespuesDeFallo = true;
        }

        private void esperarInicioWarmup() throws InterruptedException {
            inicioWarmup.await(2, TimeUnit.SECONDS);
        }
    }
}
