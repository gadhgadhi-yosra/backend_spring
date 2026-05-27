package com.elfaddoui.backend.ai.service;

import com.elfaddoui.backend.ai.dto.AiChatRequest;
import com.elfaddoui.backend.ai.dto.AiChatResponse;

public interface AiChatRagService {
    AiChatResponse chat(AiChatRequest request);
}
