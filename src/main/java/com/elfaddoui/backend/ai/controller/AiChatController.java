package com.elfaddoui.backend.ai.controller;

import com.elfaddoui.backend.ai.dto.AiChatRequest;
import com.elfaddoui.backend.ai.dto.AiChatResponse;
import com.elfaddoui.backend.ai.dto.BudgetAssistantRequest;
import com.elfaddoui.backend.ai.dto.BudgetAssistantResponse;
import com.elfaddoui.backend.ai.dto.PriceCompareResponse;
import com.elfaddoui.backend.ai.service.AiChatRagService;
import com.elfaddoui.backend.ai.service.BudgetAssistantService;
import com.elfaddoui.backend.ai.service.PriceCompareService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatRagService aiChatRagService;
    private final BudgetAssistantService budgetAssistantService;
    private final PriceCompareService priceCompareService;

    public AiChatController(AiChatRagService aiChatRagService,
                            BudgetAssistantService budgetAssistantService,
                            PriceCompareService priceCompareService) {
        this.aiChatRagService = aiChatRagService;
        this.budgetAssistantService = budgetAssistantService;
        this.priceCompareService = priceCompareService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiChatRagService.chat(request));
    }

    @PostMapping("/budget-assistant")
    public ResponseEntity<BudgetAssistantResponse> budgetAssistant(@Valid @RequestBody BudgetAssistantRequest request) {
        return ResponseEntity.ok(budgetAssistantService.buildPlan(request));
    }

    @GetMapping("/price-compare")
    public ResponseEntity<PriceCompareResponse> priceCompare(@RequestParam Long productId) {
        return ResponseEntity.ok(priceCompareService.compare(productId));
    }
}
