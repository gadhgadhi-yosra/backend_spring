package com.elfaddoui.backend.ai.service;

import com.elfaddoui.backend.ai.dto.AiChatRequest;
import com.elfaddoui.backend.ai.entity.RagChunk;

import java.util.List;

public interface AiPromptBuilder {
    String systemPrompt(String locale);
    String userPrompt(AiChatRequest request, List<RagChunk> chunks);
}
