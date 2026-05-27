package com.elfaddoui.backend.admin.controller;

import com.elfaddoui.backend.admin.dto.AdminDashboardSummaryResponse;
import com.elfaddoui.backend.admin.service.AdminBackofficeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/admin/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminDashboardController {

    private final AdminBackofficeService adminBackofficeService;

    public AdminDashboardController(AdminBackofficeService adminBackofficeService) {
        this.adminBackofficeService = adminBackofficeService;
    }

    @GetMapping("/summary")
    public AdminDashboardSummaryResponse summary() {
        return adminBackofficeService.getDashboardSummary();
    }
}
