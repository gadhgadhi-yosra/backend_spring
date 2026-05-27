package com.elfaddoui.backend.ai.service.impl;

import com.elfaddoui.backend.ai.entity.RagChunk;
import com.elfaddoui.backend.ai.repository.RagChunkRepository;
import com.elfaddoui.backend.ai.service.EmbeddingService;
import com.elfaddoui.backend.ai.service.RagRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultRagRetrievalService implements RagRetrievalService {
    private static final Logger log = LoggerFactory.getLogger(DefaultRagRetrievalService.class);

    private final EmbeddingService embeddingService;
    private final RagChunkRepository ragChunkRepository;

    public DefaultRagRetrievalService(EmbeddingService embeddingService, RagChunkRepository ragChunkRepository) {
        this.embeddingService = embeddingService;
        this.ragChunkRepository = ragChunkRepository;
    }

    @Override
    public List<RagChunk> retrieve(String message, String lang, int topK) {
        try {
            String vector = embeddingService.toPgVectorLiteral(embeddingService.embed(message));
            String normalized = normalizeLang(lang);

            List<RagChunk> chunks = ragChunkRepository.searchTopK(normalized, vector, topK);
            if (chunks.isEmpty() && !"fr".equals(normalized)) {
                chunks = ragChunkRepository.searchTopK("fr", vector, topK);
            }
            return chunks;
        } catch (Exception e) {
            log.warn("RAG retrieval failed, returning empty context", e);
            return List.of();
        }
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) return "fr";
        String normalized = lang.trim().toLowerCase();
        return switch (normalized) {
            case "fr", "en", "ar" -> normalized;
            default -> "fr";
        };
    }
}
