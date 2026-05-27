package com.elfaddoui.backend;

import com.elfaddoui.backend.appconfig.entity.AppConfigEntry;
import com.elfaddoui.backend.appconfig.repository.AppConfigRepository;
import com.elfaddoui.backend.category.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HomeIntegrationTest extends ApiIntegrationTestSupport {

    @Autowired
    private AppConfigRepository appConfigRepository;

    @Test
    void getHomeReturnsDynamicSections() throws Exception {
        Category fruits = createCategory("Fruits");
        Category drinks = createCategory("Boissons");

        createProduct("Promo Orange", fruits, new BigDecimal("5.00"), 20, 4.9, true);
        createProduct("Water Fresh", drinks, new BigDecimal("2.00"), 0, 4.8, true);
        createProduct("Apple Box", fruits, new BigDecimal("7.00"), 5, 4.7, true);

        appConfigRepository.save(new AppConfigEntry("home.locationLabel", "La Marsa"));
        appConfigRepository.save(new AppConfigEntry("home.etaLabel", "Livraison en 20 min"));
        appConfigRepository.save(new AppConfigEntry("home.deliveryAreas", "Hardimaou, Werghech, Kalaaet"));

        mockMvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationLabel", is("La Marsa")))
                .andExpect(jsonPath("$.etaLabel", is("Livraison en 20 min")))
                .andExpect(jsonPath("$.deliveryAreas[0]", is("Ghardimaoui")))
                .andExpect(jsonPath("$.deliveryAreas[1]", is("Weghech")))
                .andExpect(jsonPath("$.deliveryAreas[2]", is("Kalaa")))
                .andExpect(jsonPath("$.deals", hasSize(2)))
                .andExpect(jsonPath("$.deals[0].id").isString())
                .andExpect(jsonPath("$.deals[0].image").exists())
                .andExpect(jsonPath("$.deals[0].category").exists())
                .andExpect(jsonPath("$.deals[0].reviews").isNumber())
                .andExpect(jsonPath("$.forYou", hasSize(3)))
                .andExpect(jsonPath("$.recent", hasSize(3)));
    }
}
