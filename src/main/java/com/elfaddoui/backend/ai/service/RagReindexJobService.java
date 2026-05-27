package com.elfaddoui.backend.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class RagReindexJobService {
    private static final Logger log = LoggerFactory.getLogger(RagReindexJobService.class);

    private final AtomicReference<String> status = new AtomicReference<>("idle");
    private final AtomicReference<String> lastError = new AtomicReference<>("");
    private final RagIngestionService ingestion;

    public RagReindexJobService(RagIngestionService ingestion) {
        this.ingestion = ingestion;
    }

    @Async
    public void run() {
        status.set("running");
        lastError.set("");
        try {
            ingestion.reindexAll();
            status.set("done");
        } catch (Exception e) {
            status.set("failed");
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            String rootMsg = root.getMessage() == null ? "" : root.getMessage();
            lastError.set(e.getClass().getSimpleName() + ": " + root.getClass().getSimpleName() + ": " + rootMsg);
            log.error("RAG reindex failed", e);
        }
    }

    public String status() {
        return status.get();
    }

    public String lastError() {
        return lastError.get();
    }
}
