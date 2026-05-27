package com.elfaddoui.backend;

import com.elfaddoui.backend.category.entity.Category;
import com.elfaddoui.backend.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void authenticatedUserCanManageOwnCart() throws Exception {
        Category category = createCategory("Fruits");
        Product orange = createProduct("Orange", category, new BigDecimal("4.50"), 0, 4.8, true);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + clientToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "qty": 2
                                }
                                """.formatted(orange.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId", is(orange.getId().intValue())))
                .andExpect(jsonPath("$.items[0].qty", is(2)))
                .andExpect(jsonPath("$.totalItems", is(2)))
                .andExpect(jsonPath("$.subtotal", is(9.00)));

        mockMvc.perform(patch("/api/cart/items/{productId}", orange.getId())
                        .header("Authorization", "Bearer " + clientToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "qty": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].qty", is(5)))
                .andExpect(jsonPath("$.subtotal", is(22.50)));

        mockMvc.perform(delete("/api/cart/items/{productId}", orange.getId())
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems", is(0)))
                .andExpect(jsonPath("$.subtotal", is(0)));
    }

    @Test
    void cartIsScopedToCurrentJwtUser() throws Exception {
        Category category = createCategory("Fruits");
        Product orange = createProduct("Orange", category, new BigDecimal("4.50"), 0, 4.8, true);
        String firstUserToken = clientToken("first@elfaddoui.test");
        String secondUserToken = clientToken("second@elfaddoui.test");

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + firstUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "qty": 1
                                }
                                """.formatted(orange.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems", is(0)));
    }

    @Test
    void addAndUpdateValidateStockAndProductAvailability() throws Exception {
        Category category = createCategory("Fruits");
        Product orange = createProduct("Orange", category, new BigDecimal("4.50"), 0, 4.8, true);
        orange.setStockQty(3);
        productRepository.save(orange);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + clientToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "qty": 4
                                }
                                """.formatted(orange.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Requested quantity exceeds available stock")));

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + clientToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "qty": 2
                                }
                                """.formatted(orange.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/cart/items/{productId}", orange.getId())
                        .header("Authorization", "Bearer " + clientToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "qty": 5
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Requested quantity exceeds available stock")));

        Product archived = createProduct("Archived Orange", category, new BigDecimal("5.00"), 0, 4.0, false);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + clientToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "qty": 1
                                }
                                """.formatted(archived.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Product is inactive")));
    }

    @Test
    void clearCartRemovesOnlyCurrentUsersItems() throws Exception {
        Category category = createCategory("Fruits");
        Product orange = createProduct("Orange", category, new BigDecimal("4.50"), 0, 4.8, true);
        String firstUserToken = clientToken("first@elfaddoui.test");
        String secondUserToken = clientToken("second@elfaddoui.test");

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + firstUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "qty": 1
                                }
                                """.formatted(orange.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + secondUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "qty": 2
                                }
                                """.formatted(orange.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/cart")
                        .header("Authorization", "Bearer " + firstUserToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + firstUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].qty", is(2)));
    }
}
