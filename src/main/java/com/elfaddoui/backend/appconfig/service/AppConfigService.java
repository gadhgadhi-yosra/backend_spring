package com.elfaddoui.backend.appconfig.service;

public interface AppConfigService {
    String getValue(String key, String defaultValue);
    void setValue(String key, String value);
}
