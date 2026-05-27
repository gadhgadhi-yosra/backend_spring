package com.elfaddoui.backend.appconfig.service.impl;

import com.elfaddoui.backend.appconfig.repository.AppConfigRepository;
import com.elfaddoui.backend.appconfig.service.AppConfigService;
import com.elfaddoui.backend.appconfig.entity.AppConfigEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AppConfigServiceImpl implements AppConfigService {

    private final AppConfigRepository appConfigRepository;

    public AppConfigServiceImpl(AppConfigRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }
//Elle cherche ou lire une configuration par sa clé
    @Override
    public String getValue(String key, String defaultValue) {
        return appConfigRepository.findById(key)
                .map(config -> config.getConfigValue())
                .orElse(defaultValue);
    }
//Elle ajoute ou modifie une configuration.
    @Override
    @Transactional
    public void setValue(String key, String value) {
        appConfigRepository.save(new AppConfigEntry(key, value));
    }
}
