package com.librax.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librax.config.ApiConfig;
import com.librax.dto.GoogleVolumesResponseDto;
import com.librax.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Raw HTTP client for the Google Books API. Knows nothing about the
 * LibraX domain model — only how to build the request and deserialize
 * the JSON response into {@link GoogleVolumesResponseDto}.
 */
public class GoogleBooksClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksClient.class);

    private final ApiConfig apiConfig;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GoogleBooksClient(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(apiConfig.getTimeout())
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    public GoogleVolumesResponseDto fetchVolumes(String queryExpression) {
        String url = buildUrl(queryExpression);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "LibraX-Library-System/1.0 (academic-demo; mailto:admin@librax.edu)")
                .header("Accept", "application/json")
                .timeout(apiConfig.getTimeout())
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 429) {
                throw new ApiException("Google Books rate limit reached, please try again shortly.");
            }
            if (status < 200 || status >= 300) {
                log.warn("Google Books API returned HTTP {}: {}", status, truncate(response.body()));
                throw new ApiException("Google Books returned an error (HTTP " + status + ").");
            }
            return objectMapper.readValue(response.body(), GoogleVolumesResponseDto.class);
        } catch (java.net.http.HttpTimeoutException e) {
            throw new ApiException("Request to Google Books timed out.", e);
        } catch (java.net.ConnectException | java.net.UnknownHostException e) {
            throw new ApiException("Unable to connect to Google Books. Please check your internet connection.", e);
        } catch (IOException e) {
            throw new ApiException("Received an unexpected response from Google Books.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Google Books request was interrupted.", e);
        }
    }

    private String buildUrl(String queryExpression) {
        StringBuilder sb = new StringBuilder(apiConfig.getBaseUrl());
        sb.append("?q=").append(URLEncoder.encode(queryExpression, StandardCharsets.UTF_8));
        sb.append("&maxResults=").append(apiConfig.getMaxResults());
        apiConfig.getApiKey().ifPresent(key -> sb.append("&key=").append(URLEncoder.encode(key, StandardCharsets.UTF_8)));
        return sb.toString();
    }

    private String truncate(String s) {
        return s == null ? "" : s.substring(0, Math.min(300, s.length()));
    }
}
