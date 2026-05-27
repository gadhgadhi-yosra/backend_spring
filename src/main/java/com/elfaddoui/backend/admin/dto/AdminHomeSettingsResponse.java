package com.elfaddoui.backend.admin.dto;

import java.util.List;

public record AdminHomeSettingsResponse(
        String locationLabel,
        String etaLabel,
        List<String> deliveryAreas
) {
}
