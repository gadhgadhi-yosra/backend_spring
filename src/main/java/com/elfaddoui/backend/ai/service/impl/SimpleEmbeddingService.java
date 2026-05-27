package com.elfaddoui.backend.ai.service.impl;

import com.elfaddoui.backend.ai.config.AiProperties;
import com.elfaddoui.backend.ai.service.EmbeddingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleEmbeddingService implements EmbeddingService {
    private static final Logger log = LoggerFactory.getLogger(SimpleEmbeddingService.class);

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public SimpleEmbeddingService(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(2000, aiProperties.getTimeoutMs())))
                .build();
    }

    @Override
    public List<Double> embed(String text) {
        if (useRemoteProvider()) {
            try {
                return embedWithProvider(text);
            } catch (Exception e) {
                log.warn("Remote embeddings failed for provider={}, fallback to local deterministic embedding",
                        normalizedEmbeddingProvider(), e);
            }
        }
        return embedLocal(text);
    }

    private List<Double> embedWithProvider(String text) throws Exception {
        String payload = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                .put("model", aiProperties.getEmbeddingModel())
                .put("input", text == null ? "" : text));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(embeddingsUrl()))
                .timeout(Duration.ofMillis(Math.max(2000, aiProperties.getTimeoutMs())))
                .header("Authorization", "Bearer " + embeddingApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Embeddings failed: provider=" + normalizedEmbeddingProvider()
                    + " status=" + response.statusCode() + " body=" + response.body());
        }
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode arr = root.path("data").path(0).path("embedding");
        if (!arr.isArray() || arr.isEmpty()) {
            throw new IllegalStateException("OpenAI embeddings returned empty vector");
        }
        List<Double> values = new ArrayList<>(arr.size());
        for (JsonNode n : arr) {
            values.add(n.asDouble());
        }
        return values;
    }

    private List<Double> embedLocal(String text) {
        // Deterministic local fallback embedding so RAG still works in dev without API key.
        int dim = 1536;
        List<Double> vector = new ArrayList<>(dim);
        int seed = text == null ? 0 : text.hashCode();
        for (int i = 0; i < dim; i++) {
            int v = seed ^ (i * 265443576);
            double d = ((v & 0x7fffffff) % 10000) / 10000.0;
            vector.add(d);
        }
        return vector;
    }

    private boolean useRemoteProvider() {
        String provider = normalizedEmbeddingProvider();
        if (!"openai".equals(provider) && !"groq".equals(provider)) {
            return false;
        }
        return embeddingApiKey() != null && !embeddingApiKey().isBlank();
    }

    private String normalizedEmbeddingProvider() {
        String provider = aiProperties.getEmbeddingProvider();
        if (provider == null || provider.isBlank()) {
            provider = aiProperties.getProvider();
        }
        return provider == null ? "" : provider.trim().toLowerCase();
    }

    private String embeddingsUrl() {
        if ("groq".equals(normalizedEmbeddingProvider())) {
            return "https://api.groq.com/openai/v1/embeddings";
        }
        return "https://api.openai.com/v1/embeddings";
    }

    private String embeddingApiKey() {
        if (aiProperties.getEmbeddingApiKey() != null && !aiProperties.getEmbeddingApiKey().isBlank()) {
            return aiProperties.getEmbeddingApiKey();
        }
        return aiProperties.getApiKey();
    }
}
