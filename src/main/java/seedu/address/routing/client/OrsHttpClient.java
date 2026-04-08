package seedu.address.routing.client;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import seedu.address.routing.security.KeyDeriver;

/**
 * Lightweight HTTP client for OpenRouteService.
 * Uses only java.net — no third-party HTTP libraries needed.
 */
public class OrsHttpClient {

    /**
     * POST to an ORS endpoint, returns raw JSON response string.
     */
    public String post(String path, String jsonBody) throws IOException {
        return KeyDeriver.securePost(path, jsonBody);
    }

    /**
     * GET to an ORS endpoint, returns raw JSON response string.
     */
    public String get(String path) throws IOException {
        return KeyDeriver.secureGet(path);
    }

    /** URL-encodes a string for use in query parameters. */
    public static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
