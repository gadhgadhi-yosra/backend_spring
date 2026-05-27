package com.elfaddoui.backend.ai.service;

import com.elfaddoui.backend.ai.dto.BudgetAssistantRequest;
import com.elfaddoui.backend.ai.dto.BudgetAssistantResponse;

public interface BudgetAssistantService {
    BudgetAssistantResponse buildPlan(BudgetAssistantRequest request);
}
