package com.elfaddoui.backend;

import com.elfaddoui.backend.category.entity.Category;
import com.elfaddoui.backend.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductAdminIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void adminCanListAndFetchInactiveProducts() throws Exception {
        Category category = createCategory("Fruits");
        Product product = createProduct("Orange Cachee", category, new BigDecimal("5.00"), 0, 4.0, false);

        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(product.getId().intValue())))
                .andExpect(jsonPath("$.content[0].isActive", is(false)));

        mockMvc.perform(get("/api/admin/products/{id}", product.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(product.getId().intValue())))
                .andExpect(jsonPath("$.isActive", is(false)));
    }

    @Test
    void adminCanFilterProductsByBusinessFlags() throws Exception {
        Category bioCategory = createCategory("Fruits", "fruits-et-legumes", "Fruits & Légumes", 10);
        Category drinks = createCategory("Boissons");

        createProduct("Orange Promo", bioCategory, new BigDecimal("5.00"), 20, 4.8, true, true, true, false, true);
        createProduct("Jus Simple", drinks, new BigDecimal("4.00"), 0, 3.9, true);

        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .param("promoOnly", "true")
                        .param("bioOnly", "true")
                        .param("popularOnly", "true")
                        .param("minDiscountPct", "10")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Orange Promo")))
                .andExpect(jsonPath("$.content[0].isPromo", is(true)))
                .andExpect(jsonPath("$.content[0].isBio", is(true)))
                .andExpect(jsonPath("$.content[0].isPopular", is(true)));
    }

    @Test
    void adminCanCreateUpdateAndDeleteProduct() throws Exception {
        Category category = createCategory("Fruits");

        String createResponse = mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("name", "Orange Premium"),
                                Map.entry("description", "Orange premium importee"),
                                Map.entry("price", new BigDecimal("6.90")),
                                Map.entry("oldPrice", new BigDecimal("7.50")),
                                Map.entry("discountPct", 8),
                                Map.entry("categoryId", category.getId()),
                                Map.entry("imageUrl", "https://cdn.example.com/orange.jpg"),
                                Map.entry("stockQty", 25),
                                Map.entry("isActive", true),
                                Map.entry("isPromo", true),
                                Map.entry("rating", 4.7)
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Orange Premium")))
                .andExpect(jsonPath("$.categoryId", is(category.getId().intValue())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long productId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(put("/api/admin/products/{id}", productId)
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("name", "Orange Premium XL"),
                                Map.entry("description", "Orange premium mise a jour"),
                                Map.entry("price", new BigDecimal("7.10")),
                                Map.entry("oldPrice", new BigDecimal("7.90")),
                                Map.entry("discountPct", 10),
                                Map.entry("categoryId", category.getId()),
                                Map.entry("imageUrl", "https://cdn.example.com/orange-xl.jpg"),
                                Map.entry("stockQty", 30),
                                Map.entry("isActive", true),
                                Map.entry("isPromo", true),
                                Map.entry("rating", 4.9)
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Orange Premium XL")))
                .andExpect(jsonPath("$.discountPct", is(10)));

        mockMvc.perform(delete("/api/admin/products/{id}", productId)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void nonPromoProductClearsDiscountAndOldPrice() throws Exception {
        Category category = createCategory("Fruits");

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("name", "Orange Simple"),
                                Map.entry("description", "Orange sans promo"),
                                Map.entry("price", new BigDecimal("5.40")),
                                Map.entry("oldPrice", new BigDecimal("6.10")),
                                Map.entry("discountPct", 12),
                                Map.entry("categoryId", category.getId()),
                                Map.entry("imageUrl", "https://cdn.example.com/orange-simple.jpg"),
                                Map.entry("stockQty", 15),
                                Map.entry("isActive", true),
                                Map.entry("isPromo", false),
                                Map.entry("rating", 4.3)
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isPromo", is(false)))
                .andExpect(jsonPath("$.discountPct", is(0)))
                .andExpect(jsonPath("$.oldPrice").doesNotExist());
    }

    @Test
    void promoProductRequiresPositiveDiscountAndOldPriceAbovePrice() throws Exception {
        Category category = createCategory("Fruits");

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("name", "Orange Promo"),
                                Map.entry("description", "Orange promo invalide"),
                                Map.entry("price", new BigDecimal("5.40")),
                                Map.entry("discountPct", 0),
                                Map.entry("oldPrice", new BigDecimal("6.10")),
                                Map.entry("categoryId", category.getId()),
                                Map.entry("imageUrl", "https://cdn.example.com/orange-promo.jpg"),
                                Map.entry("stockQty", 15),
                                Map.entry("isActive", true),
                                Map.entry("isPromo", true),
                                Map.entry("rating", 4.3)
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("discountPct must be > 0 when isPromo=true")));

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("name", "Orange Promo 2"),
                                Map.entry("description", "Orange promo invalide"),
                                Map.entry("price", new BigDecimal("5.40")),
                                Map.entry("discountPct", 10),
                                Map.entry("oldPrice", new BigDecimal("5.00")),
                                Map.entry("categoryId", category.getId()),
                                Map.entry("imageUrl", "https://cdn.example.com/orange-promo-2.jpg"),
                                Map.entry("stockQty", 15),
                                Map.entry("isActive", true),
                                Map.entry("isPromo", true),
                                Map.entry("rating", 4.3)
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("oldPrice must be >= price when isPromo=true")));
    }
}
