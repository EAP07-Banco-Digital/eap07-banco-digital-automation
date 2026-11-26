package co.edu.udea.eap07.certificacion.bancodigital.technical;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class CucumberHooksTechnical {

    public static final int PUERTO = 18765;
    private static ServerSocket servidor;
    private static Thread hilo;

    private CucumberHooksTechnical() {
    }

    @BeforeAll
    public static void iniciarServidorLocal() throws IOException {
        servidor = new ServerSocket(PUERTO);
        hilo = new Thread(() -> {
            while (!servidor.isClosed()) {
                try (Socket cliente = servidor.accept()) {
                    BufferedReader entrada = new BufferedReader(
                            new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
                    String linea;
                    while ((linea = entrada.readLine()) != null && !linea.isEmpty()) {
                        // Consume headers before responding.
                    }
                    byte[] cuerpo = "{}".getBytes(StandardCharsets.UTF_8);
                    String respuesta = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: "
                            + cuerpo.length + "\r\nConnection: close\r\n\r\n";
                    OutputStream salida = cliente.getOutputStream();
                    salida.write(respuesta.getBytes(StandardCharsets.UTF_8));
                    salida.write(cuerpo);
                    salida.flush();
                } catch (IOException ignorado) {
                    if (!servidor.isClosed()) {
                        throw new IllegalStateException("Servidor técnico Cucumber detenido", ignorado);
                    }
                }
            }
        }, "servidor-cucumber-tecnico");
        hilo.setDaemon(true);
        hilo.start();
    }

    @AfterAll
    public static void detenerServidorLocal() throws IOException {
        if (servidor != null) {
            servidor.close();
        }
    }
}
