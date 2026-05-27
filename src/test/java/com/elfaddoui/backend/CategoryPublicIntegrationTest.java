package com.elfaddoui.backend;

import com.elfaddoui.backend.category.entity.Category;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryPublicIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void publicCategoriesEndpointReturnsDynamicCategorySummaries() throws Exception {
        Category fruits = createCategory("Fruits", "fruits", "Fruits", 0, true, true, false, false);
        Category drinks = createCategory("Boissons");

        createProduct("Orange Juice", drinks, new BigDecimal("4.50"), 15, 4.9, true);
        createProduct("Apple Box", fruits, new BigDecimal("8.00"), 0, 4.2, true);
        createProduct("Tomato Box", fruits, new BigDecimal("6.50"), 5, 4.7, true);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].key", is("boissons")))
                .andExpect(jsonPath("$[0].name", is("Boissons")))
                .andExpect(jsonPath("$[0].productCount", is(1)))
                .andExpect(jsonPath("$[1].key", is("fruits")))
                .andExpect(jsonPath("$[1].name", is("Fruits")))
                .andExpect(jsonPath("$[1].productCount", is(2)))
                .andExpect(jsonPath("$[1].promoCount", is(1)))
                .andExpect(jsonPath("$[1].tabs", hasSize(4)))
                .andExpect(jsonPath("$[1].chips", hasSize(3)))
                .andExpect(jsonPath("$[1].tags", hasItems("Bio", "Promos")));
    }

    @Test
    void productSearchSupportsFrontendDisplayCategoryName() throws Exception {
        Category fruits = createCategory("Fruits", "fruits-et-legumes", "Fruits & Légumes", 10);
        Category drinks = createCategory("Boissons");

        createProduct("Orange Juice", drinks, new BigDecimal("4.50"), 15, 4.9, true);
        createProduct("Apple Box", fruits, new BigDecimal("8.00"), 0, 4.2, true);

        mockMvc.perform(get("/api/products")
                        .param("categoryName", "Fruits & Légumes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Apple Box")))
                .andExpect(jsonPath("$.content[0].displayCategoryName", is("Fruits & Légumes")));
    }

    @Test
    void categoryProductsEndpointReturnsSummarySubcategoriesAndEnrichedProducts() throws Exception {
        Category fruits = createCategory("Fruits", "fruits-et-legumes", "Fruits & Légumes", 10);

        createProduct("Apple Box", fruits, new BigDecimal("8.00"), 0, 4.2, true);
        createProduct("Tomato Box", fruits, new BigDecimal("6.50"), 5, 4.7, true, true, false, false, false);

        mockMvc.perform(get("/api/categories/fruits-et-legumes/products")
                        .param("sort", "recommended"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", is("fruits-et-legumes")))
                .andExpect(jsonPath("$.name", is("Fruits & Légumes")))
                .andExpect(jsonPath("$.productCount", is(2)))
                .andExpect(jsonPath("$.subCategories", hasItems("Fruits")))
                .andExpect(jsonPath("$.tabs", hasSize(4)))
                .andExpect(jsonPath("$.chips", hasSize(3)))
                .andExpect(jsonPath("$.chips[1].label", is("1 promos")))
                .andExpect(jsonPath("$.products", hasSize(2)))
                .andExpect(jsonPath("$.products[0].categoryKey", is("fruits-et-legumes")))
                .andExpect(jsonPath("$.products[0].displayCategoryName", is("Fruits & Légumes")))
                .andExpect(jsonPath("$.products[0].reviews", notNullValue()))
                .andExpect(jsonPath("$.products[0].score", notNullValue()));
    }

    @Test
    void categoryProductsEndpointSupportsPresetFilters() throws Exception {
        Category fruits = createCategory("Fruits", "fruits-et-legumes", "Fruits & Légumes", 10);

        createProduct("Apple Box", fruits, new BigDecimal("8.00"), 0, 4.2, true);
        createProduct("Tomato Box", fruits, new BigDecimal("6.50"), 5, 4.7, true, true, false, false, false);

        mockMvc.perform(get("/api/categories/fruits-et-legumes/products")
                        .param("preset", "promo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCount", is(1)))
                .andExpect(jsonPath("$.chips[1].selected", is(true)));
    }
}
