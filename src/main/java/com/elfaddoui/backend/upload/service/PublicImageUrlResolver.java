package com.elfaddoui.backend.upload.service;

import com.elfaddoui.backend.config.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class PublicImageUrlResolver {

    private final AppProperties appProperties;

    public PublicImageUrlResolver(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String resolve(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }

        String trimmed = imageUrl.trim();
        if (isAbsoluteUrl(trimmed)) {
            return trimmed;
        }

        String normalizedPath = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
        String requestBaseUrl = currentRequestBaseUrl();
        if (requestBaseUrl != null) {
            return requestBaseUrl + normalizedPath;
        }

        String configuredBaseUrl = appProperties.getUploads().getPublicBaseUrl();
        if (configuredBaseUrl == null || configuredBaseUrl.isBlank()) {
            return normalizedPath;
        }

        String configured = configuredBaseUrl.trim();
        if (isAbsoluteUrl(configured)) {
            if (configured.endsWith("/uploads") && normalizedPath.startsWith("/uploads/")) {
                return configured + normalizedPath.substring("/uploads".length());
            }
            return configured.endsWith("/")
                    ? configured.substring(0, configured.length() - 1) + normalizedPath
                    : configured + normalizedPath;
        }

        if (configured.endsWith("/uploads") && normalizedPath.startsWith("/uploads/")) {
            return configured + normalizedPath.substring("/uploads".length());
        }
        return configured.endsWith("/")
                ? configured.substring(0, configured.length() - 1) + normalizedPath
                : configured + normalizedPath;
    }

    private boolean isAbsoluteUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String currentRequestBaseUrl() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
    }
}
