package com.elfaddoui.backend.admin.controller;

import com.elfaddoui.backend.admin.dto.AdminDeliverySettingsRequest;
import com.elfaddoui.backend.admin.dto.AdminDeliverySettingsResponse;
import com.elfaddoui.backend.admin.service.AdminBackofficeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin/delivery/settings", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminDeliverySettingsController {

    private final AdminBackofficeService adminBackofficeService;

    public AdminDeliverySettingsController(AdminBackofficeService adminBackofficeService) {
        this.adminBackofficeService = adminBackofficeService;
    }

    @GetMapping
    public AdminDeliverySettingsResponse get() {
        return adminBackofficeService.getDeliverySettings();
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public AdminDeliverySettingsResponse update(@Valid @RequestBody AdminDeliverySettingsRequest request) {
        return adminBackofficeService.updateDeliverySettings(request);
    }
}
