package com.elfaddoui.backend.ai.service;

public interface LlmChatService {
    String chatJson(String systemPrompt, String userPrompt);
}
