package com.elfaddoui.backend.admin.controller;

import com.elfaddoui.backend.admin.dto.AdminHomeSettingsRequest;
import com.elfaddoui.backend.admin.dto.AdminHomeSettingsResponse;
import com.elfaddoui.backend.appconfig.service.AppConfigService;
import com.elfaddoui.backend.config.AppProperties;
import com.elfaddoui.backend.home.util.DeliveryAreaNormalizer;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/api/admin/home/settings", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminHomeSettingsController {

    private static final String DEFAULT_AREAS = "Ghardimaoui,Weghech,Kalaa";

    private final AppConfigService appConfigService;
    private final AppProperties appProperties;

    public AdminHomeSettingsController(AppConfigService appConfigService, AppProperties appProperties) {
        this.appConfigService = appConfigService;
        this.appProperties = appProperties;
    }

    @GetMapping
    public AdminHomeSettingsResponse get() {
        return response();
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public AdminHomeSettingsResponse update(@Valid @RequestBody AdminHomeSettingsRequest request) {
        appConfigService.setValue("home.locationLabel", request.locationLabel().trim());
        appConfigService.setValue("home.etaLabel", request.etaLabel().trim());
        appConfigService.setValue("home.deliveryAreas", normalizeAreas(request.deliveryAreas()));
        return response();
    }

    private AdminHomeSettingsResponse response() {
        return new AdminHomeSettingsResponse(
                appConfigService.getValue("home.locationLabel", appProperties.getHome().getLocationLabel()),
                appConfigService.getValue("home.etaLabel", appProperties.getHome().getEtaLabel()),
                parseAreas(appConfigService.getValue("home.deliveryAreas", DEFAULT_AREAS))
        );
    }

    private String normalizeAreas(String raw) {
        List<String> areas = parseAreas(raw == null ? DEFAULT_AREAS : raw);
        return String.join(",", areas.isEmpty() ? parseAreas(DEFAULT_AREAS) : areas);
    }

    private List<String> parseAreas(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(DeliveryAreaNormalizer::canonicalize)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }
}
