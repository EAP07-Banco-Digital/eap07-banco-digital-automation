package co.edu.udea.eap07.certificacion.bancodigital.utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;

public final class DatosDePrueba {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private DatosDePrueba() {
    }

    public static String identificadorUnico() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);
    }

    public static String correoUnico() {
        return "qa." + identificadorUnico() + "@example.com";
    }

    public static String documentoUnico() {
        return new BigInteger(128, ALEATORIO).toString();
    }
}
