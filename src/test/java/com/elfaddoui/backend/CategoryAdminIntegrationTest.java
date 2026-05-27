package com.elfaddoui.backend;

import com.elfaddoui.backend.category.entity.Category;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryAdminIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void adminCategoriesExposeBusinessStats() throws Exception {
        Category fruits = createCategory("Fruits", "fruits-et-legumes", "Fruits & Légumes", 10, false, true, false, false);
        createProduct("Apple Box", fruits, new BigDecimal("8.00"), 0, 4.2, true, false, true, false, true);
        createProduct("Tomato Box", fruits, new BigDecimal("6.50"), 15, 4.7, true, true, true, false, true);

        mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].key", is("fruits-et-legumes")))
                .andExpect(jsonPath("$.content[0].productCount", is(2)))
                .andExpect(jsonPath("$.content[0].promoProductCount", is(1)))
                .andExpect(jsonPath("$.content[0].bioProductCount", is(2)))
                .andExpect(jsonPath("$.content[0].popularProductCount", is(2)))
                .andExpect(jsonPath("$.content[0].maxDiscountPct", is(15)))
                .andExpect(jsonPath("$.content[0].tags", hasItems("promo", "bio", "popular")));
    }
}
