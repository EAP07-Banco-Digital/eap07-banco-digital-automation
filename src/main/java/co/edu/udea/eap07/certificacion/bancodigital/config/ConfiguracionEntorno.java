package co.edu.udea.eap07.certificacion.bancodigital.config;

import java.net.URI;

/**
 * Resuelve la configuración no sensible del entorno y las credenciales
 * administrativas únicamente cuando son solicitadas.
 */
public final class ConfiguracionEntorno {

    private static final String DEFAULT_BASE_URL =
            "https://eap07-banco-digital-backend.onrender.com";
    private static final int DEFAULT_HTTP_CONNECT_TIMEOUT_MS = 10_000;
    private static final int DEFAULT_HTTP_READ_TIMEOUT_MS = 30_000;
    private static final int DEFAULT_WARMUP_MAX_ATTEMPTS = 5;
    private static final int DEFAULT_WARMUP_RETRY_DELAY_MS = 10_000;
    private static final int DEFAULT_WARMUP_READ_TIMEOUT_MS = 60_000;

    private final String baseUrl;
    private final int httpConnectTimeoutMs;
    private final int httpReadTimeoutMs;
    private final int warmupMaxAttempts;
    private final int warmupRetryDelayMs;
    private final int warmupReadTimeoutMs;

    private ConfiguracionEntorno(String baseUrl,
                                 int httpConnectTimeoutMs,
                                 int httpReadTimeoutMs,
                                 int warmupMaxAttempts,
                                 int warmupRetryDelayMs,
                                 int warmupReadTimeoutMs) {
        this.baseUrl = baseUrl;
        this.httpConnectTimeoutMs = httpConnectTimeoutMs;
        this.httpReadTimeoutMs = httpReadTimeoutMs;
        this.warmupMaxAttempts = warmupMaxAttempts;
        this.warmupRetryDelayMs = warmupRetryDelayMs;
        this.warmupReadTimeoutMs = warmupReadTimeoutMs;
    }

    public static ConfiguracionEntorno desdeEntorno() {
        return new ConfiguracionEntorno(
                resolveBaseUrl(),
                positiveInteger("HTTP_CONNECT_TIMEOUT_MS", DEFAULT_HTTP_CONNECT_TIMEOUT_MS),
                positiveInteger("HTTP_READ_TIMEOUT_MS", DEFAULT_HTTP_READ_TIMEOUT_MS),
                positiveInteger("WARMUP_MAX_ATTEMPTS", DEFAULT_WARMUP_MAX_ATTEMPTS),
                positiveInteger("WARMUP_RETRY_DELAY_MS", DEFAULT_WARMUP_RETRY_DELAY_MS),
                positiveInteger("WARMUP_READ_TIMEOUT_MS", DEFAULT_WARMUP_READ_TIMEOUT_MS));
    }

    public String baseUrl() {
        return baseUrl;
    }

    public int httpConnectTimeoutMs() {
        return httpConnectTimeoutMs;
    }

    public int httpReadTimeoutMs() {
        return httpReadTimeoutMs;
    }

    public int warmupMaxAttempts() {
        return warmupMaxAttempts;
    }

    public int warmupRetryDelayMs() {
        return warmupRetryDelayMs;
    }

    public int warmupReadTimeoutMs() {
        return warmupReadTimeoutMs;
    }

    public String adminEmail() {
        return requiredSecret("ADMIN_EMAIL");
    }

    public String adminPassword() {
        return requiredSecret("ADMIN_PASSWORD");
    }

    @Override
    public String toString() {
        return "ConfiguracionEntorno{" +
                "baseUrl='" + baseUrl + '\'' +
                ", httpConnectTimeoutMs=" + httpConnectTimeoutMs +
                ", httpReadTimeoutMs=" + httpReadTimeoutMs +
                ", warmupMaxAttempts=" + warmupMaxAttempts +
                ", warmupRetryDelayMs=" + warmupRetryDelayMs +
                ", warmupReadTimeoutMs=" + warmupReadTimeoutMs +
                '}';
    }

    private static String resolveBaseUrl() {
        String configured = System.getenv("BASE_URL");
        if (configured == null) {
            configured = DEFAULT_BASE_URL;
        }
        if (configured.isBlank()) {
            throw invalid("BASE_URL", "no puede estar vacío");
        }

        try {
            URI uri = URI.create(configured.trim());
            boolean supportedScheme = "http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme());
            if (!supportedScheme || uri.getHost() == null || uri.getUserInfo() != null
                    || uri.getQuery() != null || uri.getFragment() != null) {
                throw invalid("BASE_URL", "debe ser una URL HTTP/HTTPS con host válido y sin credenciales");
            }
            return removeTrailingSlashes(uri.toString());
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("BASE_URL")) {
                throw exception;
            }
            throw invalid("BASE_URL", "no tiene un formato válido");
        }
    }

    private static int positiveInteger(String variable, int defaultValue) {
        String configured = System.getenv(variable);
        if (configured == null) {
            return defaultValue;
        }
        if (configured.isBlank()) {
            throw invalid(variable, "debe ser un entero positivo");
        }
        try {
            int value = Integer.parseInt(configured.trim());
            if (value <= 0) {
                throw invalid(variable, "debe ser un entero positivo");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw invalid(variable, "debe ser un entero positivo");
        }
    }

    private static String requiredSecret(String variable) {
        String value = System.getenv(variable);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("La variable de entorno " + variable
                    + " es obligatoria para esta operación administrativa");
        }
        return value;
    }

    private static String removeTrailingSlashes(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }

    private static IllegalArgumentException invalid(String variable, String reason) {
        return new IllegalArgumentException("Valor inválido para " + variable + ": " + reason);
    }
}
