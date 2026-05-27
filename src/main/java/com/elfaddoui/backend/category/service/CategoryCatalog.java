package com.elfaddoui.backend.category.service;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CategoryCatalog {

    public String keyFor(String input) {
        return normalize(input).replace(' ', '-');
    }

    public String displayNameFor(String input) {
        String normalized = normalize(input);
        if (normalized.isEmpty()) {
            return "";
        }
        String[] parts = normalized.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) {
                builder.append(parts[i].substring(1));
            }
        }
        return builder.toString();
    }

    public String coverImageFor(String input, String fallback) {
        return (fallback == null || fallback.isBlank()) ? null : fallback;
    }

    public String normalize(String input) {
        if (input == null) {
            return "";
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT)
                .replace('à', 'a')
                .replace('á', 'a')
                .replace('â', 'a')
                .replace('ä', 'a')
                .replace('ã', 'a')
                .replace('å', 'a')
                .replace('è', 'e')
                .replace('é', 'e')
                .replace('ê', 'e')
                .replace('ë', 'e')
                .replace('ì', 'i')
                .replace('í', 'i')
                .replace('î', 'i')
                .replace('ï', 'i')
                .replace('ò', 'o')
                .replace('ó', 'o')
                .replace('ô', 'o')
                .replace('ö', 'o')
                .replace('õ', 'o')
                .replace('ù', 'u')
                .replace('ú', 'u')
                .replace('û', 'u')
                .replace('ü', 'u')
                .replace('ç', 'c')
                .replace('ñ', 'n');
        return normalized.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }
}
