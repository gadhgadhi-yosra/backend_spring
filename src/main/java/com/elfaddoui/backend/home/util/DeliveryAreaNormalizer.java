package com.elfaddoui.backend.home.util;

import java.util.Locale;

public final class DeliveryAreaNormalizer {

    private DeliveryAreaNormalizer() {
    }

    public static String canonicalize(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.isBlank()) {
            return "";
        }

        String key = trimmed.toLowerCase(Locale.ROOT)
                .replace("'", "")
                .replace("-", " ")
                .replace("_", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return switch (key) {
            case "hardimaou", "ghardimaoui" -> "Ghardimaoui";
            case "werghech", "weghech" -> "Weghech";
            case "kalaaet", "kalaa" -> "Kalaa";
            default -> trimmed;
        };
    }
}

