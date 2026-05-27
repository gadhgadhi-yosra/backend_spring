package com.elfaddoui.backend.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public class RagProperties {
    private int topK = 8;
    private int rerankK = 4;

    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
    public int getRerankK() { return rerankK; }
    public void setRerankK(int rerankK) { this.rerankK = rerankK; }
}
