package com.elfaddoui.backend.ai.service;

import java.util.List;

public interface EmbeddingService {
    List<Double> embed(String text);

    default String toPgVectorLiteral(List<Double> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(values.get(i));
        }
        sb.append(']');
        return sb.toString();
    }
}
