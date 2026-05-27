package com.elfaddoui.backend.ai.service;

import com.elfaddoui.backend.ai.entity.RagChunk;

import java.util.List;

public interface RagRetrievalService {
    List<RagChunk> retrieve(String message, String lang, int topK);
}
