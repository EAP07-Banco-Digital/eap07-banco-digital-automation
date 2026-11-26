package co.edu.udea.eap07.certificacion.bancodigital.integrations;

import co.edu.udea.eap07.certificacion.bancodigital.config.ConfiguracionEntorno;
import co.edu.udea.eap07.certificacion.bancodigital.utils.Endpoints;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class WarmupServicio {

    private static final ConcurrentMap<String, Object> BLOQUEOS_POR_DESTINO = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Boolean> DESTINOS_CALIENTES = new ConcurrentHashMap<>();

    private WarmupServicio() {
    }

    public static void ejecutarUnaVez(ConfiguracionEntorno entorno) {
        ejecutarUnaVezEn(entorno, entorno.baseUrl());
    }

    public static void ejecutarUnaVezEn(ConfiguracionEntorno entorno, String baseUrl) {
        String claveDestino = claveDestino(baseUrl, Endpoints.DOCUMENTACION_API);
        Object bloqueo = BLOQUEOS_POR_DESTINO.computeIfAbsent(claveDestino, clave -> new Object());
        synchronized (bloqueo) {
            if (DESTINOS_CALIENTES.containsKey(claveDestino)) {
                return;
            }
            ejecutarEn(entorno, baseUrl, Endpoints.DOCUMENTACION_API);
            DESTINOS_CALIENTES.put(claveDestino, Boolean.TRUE);
        }
    }

    public static void ejecutar(ConfiguracionEntorno entorno, String ruta) {
        ejecutarEn(entorno, entorno.baseUrl(), ruta);
    }

    public static void ejecutarEn(ConfiguracionEntorno entorno, String baseUrl, String ruta) {
        URI uri;
        try {
            uri = URI.create(baseUrl + ruta);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("La URL de warmup no es válida", exception);
        }

        HttpClient cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(entorno.httpConnectTimeoutMs()))
                .build();

        RuntimeException ultimoError = null;
        for (int intento = 1; intento <= entorno.warmupMaxAttempts(); intento++) {
            try {
                HttpRequest solicitud = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofMillis(entorno.warmupReadTimeoutMs()))
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                HttpResponse<Void> respuesta = cliente.send(
                        solicitud, HttpResponse.BodyHandlers.discarding());

                if (respuesta.statusCode() >= 200 && respuesta.statusCode() < 300) {
                    return;
                }
                if (respuesta.statusCode() < 500) {
                    throw new IllegalStateException("Warmup rechazado con HTTP " + respuesta.statusCode());
                }
                ultimoError = new IllegalStateException(
                        "Warmup transitorio con HTTP " + respuesta.statusCode());
            } catch (IOException exception) {
                ultimoError = new IllegalStateException("Error transitorio de conexión durante el warmup", exception);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Warmup interrumpido", exception);
            }

            if (intento < entorno.warmupMaxAttempts()) {
                try {
                    Thread.sleep(entorno.warmupRetryDelayMs());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Espera del warmup interrumpida", exception);
                }
            }
        }

        throw new IllegalStateException(
                "Warmup agotado después de " + entorno.warmupMaxAttempts() + " intentos", ultimoError);
    }

    private static String claveDestino(String baseUrl, String ruta) {
        String baseNormalizada = baseUrl.trim().replaceAll("/+$", "");
        String rutaNormalizada = ruta.startsWith("/") ? ruta : "/" + ruta;
        return baseNormalizada + rutaNormalizada;
    }
}
