package com.elfaddoui.backend.appconfig.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_config")
public class AppConfigEntry {

    @Id
    @Column(nullable = false, unique = true)
    private String configKey;

    @Column(nullable = false, length = 2000)
    private String configValue;

    public AppConfigEntry() {
    }

    public AppConfigEntry(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
}
