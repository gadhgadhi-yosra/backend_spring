package com.elfaddoui.backend.ai.service.impl;

import com.elfaddoui.backend.ai.dto.AiChatRequest;
import com.elfaddoui.backend.ai.entity.RagChunk;
import com.elfaddoui.backend.ai.service.AiPromptBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimpleAiPromptBuilder implements AiPromptBuilder {
    @Override
    public String systemPrompt(String locale) {
        return """
            You are a retail assistant. Reply ONLY using provided context.
            Never invent prices, stock, promo dates, or loyalty rules.
            If data is missing, say it clearly.
            Output must be JSON with fields: reply, confidence, actions.
            Language must follow locale: %s.
        """.formatted(locale == null ? "fr" : locale);
    }

    @Override
    public String userPrompt(AiChatRequest request, List<RagChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        sb.append("User message: ").append(request.getMessage()).append('\n');
        sb.append("Locale: ").append(request.getLocale()).append('\n');
        if (request.getContext() != null) {
            sb.append("Loyalty balance: ").append(request.getContext().getLoyaltyBalance()).append('\n');
            sb.append("Cart product ids: ").append(request.getContext().getCartProductIds()).append('\n');
            sb.append("Favorite product ids: ").append(request.getContext().getFavoriteProductIds()).append('\n');
        }
        sb.append("Context chunks:\n");
        for (RagChunk c : chunks) {
            sb.append("- [").append(c.getDocId()).append("] ")
                    .append(c.getTitle() == null ? "" : c.getTitle()).append(" :: ")
                    .append(c.getContent()).append('\n');
        }
        return sb.toString();
    }
}
