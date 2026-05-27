package com.elfaddoui.backend.ai.controller;

import com.elfaddoui.backend.ai.service.RagReindexJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/rag")
public class AdminRagController {

    private final RagReindexJobService ragReindexJobService;

    public AdminRagController(RagReindexJobService ragReindexJobService) {
        this.ragReindexJobService = ragReindexJobService;
    }

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindex() {
        ragReindexJobService.run();
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }

    @GetMapping("/reindex/status")
    public Map<String, Object> reindexStatus() {
        String status = ragReindexJobService.status();
        String error = ragReindexJobService.lastError();
        if ("failed".equals(status) && error != null && !error.isBlank()) {
            return Map.of("status", status, "error", error);
        }
        return Map.of("status", status);
    }
}
