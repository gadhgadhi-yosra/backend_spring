package com.elfaddoui.backend.appconfig.repository;

import com.elfaddoui.backend.appconfig.entity.AppConfigEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfigEntry, String> {
}
