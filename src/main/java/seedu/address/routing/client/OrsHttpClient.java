package seedu.address.routing.client;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import seedu.address.routing.security.KeyDeriver;

/**
 * Lightweight HTTP client for OpenRouteService.
 * Uses only java.net — no third-party HTTP libraries needed.
 * All authenticated requests are routed through {@link KeyDeriver}
 * so that the API key is never held in JVM memory.
 */
public class OrsHttpClient {

    /**
     * POST to an ORS endpoint, returns raw JSON response string.
     * The request is executed in a secure subprocess — the key is never
     * assembled in JVM memory.
     *
     * @param path     the ORS API path e.g. {@code /optimization}
     * @param jsonBody the JSON request body
     * @return raw JSON response string
     * @throws IOException if the request fails
     */
    public String post(String path, String jsonBody) throws IOException {
        return KeyDeriver.securePost(path, jsonBody);
    }

    /**
     * GET to an ORS endpoint, returns raw JSON response string.
     * The request is executed in a secure subprocess — the key is never
     * assembled in JVM memory.
     *
     * @param path the ORS API path e.g. {@code /geocode/search?text=...}
     * @return raw JSON response string
     * @throws IOException if the request fails
     */
    public String get(String path) throws IOException {
        return KeyDeriver.secureGet(path);
    }

    /**
     * URL-encodes a string for use in query parameters.
     *
     * @param s the string to encode
     * @return URL-encoded string
     */
    public static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
