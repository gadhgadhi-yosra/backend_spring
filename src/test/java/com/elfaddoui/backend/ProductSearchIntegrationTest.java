package com.elfaddoui.backend;

import com.elfaddoui.backend.category.entity.Category;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductSearchIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void searchAndFilterEndpointSupportsQueryCategoryPromoPriceSortAndPagination() throws Exception {
        Category fruits = createCategory("Fruits");
        Category drinks = createCategory("Boissons");

        createProduct("Orange Juice", drinks, new BigDecimal("4.50"), 15, 4.9, true);
        createProduct("Orange Box", fruits, new BigDecimal("8.00"), 0, 4.2, true);
        createProduct("Apple Juice", drinks, new BigDecimal("3.20"), 12, 4.7, true);
        createProduct("Hidden Product", drinks, new BigDecimal("1.50"), 50, 5.0, false);

        mockMvc.perform(get("/api/products")
                        .param("query", "orange")
                        .param("category", String.valueOf(drinks.getId()))
                        .param("promoOnly", "true")
                        .param("minPrice", "4")
                        .param("maxPrice", "5")
                        .param("sort", "priceDesc")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Orange Juice")))
                .andExpect(jsonPath("$.content[0].categoryId", is(drinks.getId().intValue())))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void searchEndpointSupportsCategoryNameFilter() throws Exception {
        Category fruits = createCategory("Fruits", "fruits-et-legumes", "Fruits & Légumes", 10);
        Category drinks = createCategory("Boissons");

        createProduct("Orange Juice", drinks, new BigDecimal("4.50"), 15, 4.9, true);
        createProduct("Apple Box", fruits, new BigDecimal("8.00"), 0, 4.2, true);

        mockMvc.perform(get("/api/products")
                        .param("categoryName", "Boissons")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Orange Juice")))
                .andExpect(jsonPath("$.content[0].categoryName", is("Boissons")));
    }

    @Test
    void searchEndpointSupportsFrontendCategoryKeyAndSortAliases() throws Exception {
        Category fruits = createCategory("Fruits", "fruits-et-legumes", "Fruits & Légumes", 10);

        createProduct("Apple Box", fruits, new BigDecimal("8.00"), 0, 4.2, true);
        createProduct("Tomato Box", fruits, new BigDecimal("6.50"), 10, 4.7, true);

        mockMvc.perform(get("/api/products")
                        .param("categoryKey", "fruits-et-legumes")
                        .param("sort", "priceLow")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("Tomato Box")))
                .andExpect(jsonPath("$.content[0].categoryKey", is("fruits-et-legumes")))
                .andExpect(jsonPath("$.content[0].displayCategoryName", is("Fruits & Légumes")))
                .andExpect(jsonPath("$.content[0].isPromo", is(true)))
                .andExpect(jsonPath("$.content[0].reviews", is(141)));
    }

    @Test
    void productDetailsEndpointReturnsEnrichedFrontendFields() throws Exception {
        Category drinks = createCategory("Boissons");
        var product = createProduct("Orange Juice", drinks, new BigDecimal("4.50"), 15, 4.9, true, true, false, false, true);

        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(product.getId().intValue())))
                .andExpect(jsonPath("$.categoryKey", is("boissons")))
                .andExpect(jsonPath("$.displayCategoryName", is("Boissons")))
                .andExpect(jsonPath("$.isPromo", is(true)))
                .andExpect(jsonPath("$.isPopular", is(true)))
                .andExpect(jsonPath("$.reviews", is(147)))
                .andExpect(jsonPath("$.score", is(100)));
    }
}
