package com.elfaddoui.backend.home.config;

import com.elfaddoui.backend.appconfig.entity.AppConfigEntry;
import com.elfaddoui.backend.appconfig.repository.AppConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class HomeDataSeeder {

    @Bean
    CommandLineRunner seedHomeData(
            AppConfigRepository appConfigRepository
    ) {
        return args -> {
            if (!appConfigRepository.existsById("home.locationLabel")) {
                appConfigRepository.save(new AppConfigEntry("home.locationLabel", "Tunis, Centre Ville"));
            }
            if (!appConfigRepository.existsById("home.etaLabel")) {
                appConfigRepository.save(new AppConfigEntry("home.etaLabel", "Livraison en 35 min"));
            }
        };
    }
}
