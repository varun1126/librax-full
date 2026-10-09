package com.librax.config;

import java.time.Duration;
import java.util.Optional;

/**
 * Configuration for the external Google Books API integration.
 *
 * <p>The API key is intentionally read with the environment variable
 * {@code LIBRAX_GOOGLE_BOOKS_API_KEY} taking priority over the
 * {@code api.googleBooks.apiKey} property, so a real key never needs to
 * be committed to {@code application.properties} in source control (see
 * {@code API.md} §10 and {@code RULES.md} §10).</p>
 */
public final class ApiConfig {

    private static final String API_KEY_ENV_VAR = "LIBRAX_GOOGLE_BOOKS_API_KEY";

    private final String baseUrl;
    private final String apiKey; // may be blank; public search works without a key
    private final Duration timeout;
    private final int maxResults;

    public ApiConfig(AppConfig appConfig) {
        this.baseUrl = appConfig.getString(
                "api.googleBooks.baseUrl", "https://www.googleapis.com/books/v1/volumes");
        this.apiKey = resolveApiKey(appConfig);
        this.timeout = Duration.ofSeconds(appConfig.getInt("api.googleBooks.timeoutSeconds", 10));
        this.maxResults = appConfig.getInt("api.googleBooks.maxResults", 20);
    }

    private static String resolveApiKey(AppConfig appConfig) {
        String fromEnv = System.getenv(API_KEY_ENV_VAR);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        return appConfig.getString("api.googleBooks.apiKey", "");
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @return the configured API key, if any. Empty when unauthenticated
     *         public search should be used.
     */
    public Optional<String> getApiKey() {
        return apiKey == null || apiKey.isBlank() ? Optional.empty() : Optional.of(apiKey);
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getMaxResults() {
        return maxResults;
    }
}
