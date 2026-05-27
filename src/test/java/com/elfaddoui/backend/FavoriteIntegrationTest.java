package com.elfaddoui.backend;

import com.elfaddoui.backend.category.entity.Category;
import com.elfaddoui.backend.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FavoriteIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void authenticatedUserCanAddAndListFavorites() throws Exception {
        Category category = createCategory("Fruits");
        Product orange = createProduct("Orange", category, new BigDecimal("4.50"), 10, 4.8, true);
        Product apple = createProduct("Apple", category, new BigDecimal("3.20"), 0, 4.2, true);

        mockMvc.perform(post("/api/favorites/{productId}", orange.getId())
                        .header("Authorization", "Bearer " + clientToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId", is(orange.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Orange")))
                .andExpect(jsonPath("$.imageUrl", is("https://cdn.example.com/orange.jpg")));

        mockMvc.perform(post("/api/favorites/{productId}", apple.getId())
                        .header("Authorization", "Bearer " + clientToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId", is(apple.getId().intValue())));

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productId", is(apple.getId().intValue())))
                .andExpect(jsonPath("$[1].productId", is(orange.getId().intValue())));
    }

    @Test
    void addFavoriteIsIdempotentForSameUserAndProduct() throws Exception {
        Category category = createCategory("Fruits");
        Product orange = createProduct("Orange", category, new BigDecimal("4.50"), 10, 4.8, true);

        mockMvc.perform(post("/api/favorites/{productId}", orange.getId())
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/favorites/{productId}", orange.getId())
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void toggleAndDeleteEndpointsUpdateFavoriteState() throws Exception {
        Category category = createCategory("Fruits");
        Product orange = createProduct("Orange", category, new BigDecimal("4.50"), 10, 4.8, true);

        mockMvc.perform(post("/api/favorites/{productId}/toggle", orange.getId())
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite", is(true)))
                .andExpect(jsonPath("$.item.productId", is(orange.getId().intValue())));

        mockMvc.perform(post("/api/favorites/{productId}/toggle", orange.getId())
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite", is(false)))
                .andExpect(jsonPath("$.item", nullValue()));

        mockMvc.perform(post("/api/favorites/{productId}", orange.getId())
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/favorites/{productId}", orange.getId())
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void cannotFavoriteInactiveProduct() throws Exception {
        Category category = createCategory("Fruits");
        Product archived = createProduct("Archived Orange", category, new BigDecimal("4.50"), 10, 4.8, false);

        mockMvc.perform(post("/api/favorites/{productId}", archived.getId())
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Product is inactive")));
    }

    @Test
    void favoritesAreScopedToCurrentJwtUser() throws Exception {
        Category category = createCategory("Fruits");
        Product orange = createProduct("Orange", category, new BigDecimal("4.50"), 10, 4.8, true);
        String firstUserToken = clientToken("first@elfaddoui.test");
        String secondUserToken = clientToken("second@elfaddoui.test");

        mockMvc.perform(post("/api/favorites/{productId}", orange.getId())
                        .header("Authorization", "Bearer " + firstUserToken))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
