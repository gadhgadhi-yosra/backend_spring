package com.elfaddoui.backend.ai.service.impl;

import com.elfaddoui.backend.ai.config.AiProperties;
import com.elfaddoui.backend.ai.service.LlmChatService;
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

@Service
public class ProviderLlmChatService implements LlmChatService {
    private static final Logger log = LoggerFactory.getLogger(ProviderLlmChatService.class);

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ProviderLlmChatService(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(2000, aiProperties.getTimeoutMs())))
                .build();
    }

    @Override
    public String chatJson(String systemPrompt, String userPrompt) {
        if (useRemoteProvider()) {
            try {
                return chatWithProvider(systemPrompt, userPrompt);
            } catch (Exception e) {
                log.warn("Remote LLM call failed, using deterministic fallback JSON", e);
            }
        }
        // Keep a valid JSON payload even when no provider is configured.
        return """
                {
                  \"reply\": \"Je n'ai pas assez d'info exacte pour répondre maintenant.\",
                  \"confidence\": 0.35,
                  \"actions\": [{\"type\":\"open_category\",\"value\":\"catalogue\"}]
                }
                """;
    }

    private String chatWithProvider(String systemPrompt, String userPrompt) throws Exception {
        var root = objectMapper.createObjectNode();
        root.put("model", aiProperties.getChatModel());
        root.put("temperature", 0.2);
        var messages = root.putArray("messages");
        messages.addObject().put("role", "system").put("content", systemPrompt);
        String jsonStrictUserPrompt = userPrompt + "\n\n"
                + "Réponds STRICTEMENT en JSON valide avec la forme: "
                + "{\"reply\":\"...\",\"confidence\":0.0,\"actions\":[{\"type\":\"...\",\"value\":\"...\"}]}. "
                + "Aucun texte hors JSON.";
        messages.addObject().put("role", "user").put("content", jsonStrictUserPrompt);

        if (!isGroqProvider()) {
            root.putObject("response_format").put("type", "json_object");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(chatCompletionsUrl()))
                .timeout(Duration.ofMillis(Math.max(2000, aiProperties.getTimeoutMs())))
                .header("Authorization", "Bearer " + aiProperties.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(root)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("LLM chat failed: HTTP " + response.statusCode() + " body=" + response.body());
        }
        JsonNode json = objectMapper.readTree(response.body());
        String content = json.path("choices").path(0).path("message").path("content").asText();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("LLM chat returned empty content");
        }
        return content;
    }

    private boolean useRemoteProvider() {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            return false;
        }
        String provider = aiProperties.getProvider() == null ? "" : aiProperties.getProvider().trim().toLowerCase();
        return "openai".equals(provider) || "groq".equals(provider);
    }

    private String chatCompletionsUrl() {
        if (isGroqProvider()) {
            return "https://api.groq.com/openai/v1/chat/completions";
        }
        return "https://api.openai.com/v1/chat/completions";
    }

    private boolean isGroqProvider() {
        String provider = aiProperties.getProvider() == null ? "" : aiProperties.getProvider().trim().toLowerCase();
        return "groq".equals(provider);
    }
}
