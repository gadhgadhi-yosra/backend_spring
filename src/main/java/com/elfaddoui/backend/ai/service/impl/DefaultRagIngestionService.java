package com.elfaddoui.backend.ai.service.impl;

import com.elfaddoui.backend.ai.service.EmbeddingService;
import com.elfaddoui.backend.ai.service.RagIngestionService;
import com.elfaddoui.backend.category.repository.CategoryRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyGiftRepository;
import com.elfaddoui.backend.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultRagIngestionService implements RagIngestionService {
    private static final Logger log = LoggerFactory.getLogger(DefaultRagIngestionService.class);
    private static final int MAX_RECIPE_DOCS = 250;
    private static final int MAX_SPOON_RECIPES = 200;
    private static final String MEALDB_BY_LETTER = "https://www.themealdb.com/api/json/v1/1/search.php?f=";
    private static final String SPOON_COMPLEX_SEARCH = "https://api.spoonacular.com/recipes/complexSearch?number=50&addRecipeInformation=true&fillIngredients=true&instructionsRequired=true";

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingService embeddingService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LoyaltyGiftRepository loyaltyGiftRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final TransactionTemplate transactionTemplate;

    public DefaultRagIngestionService(JdbcTemplate jdbcTemplate,
                                      EmbeddingService embeddingService,
                                      ProductRepository productRepository,
                                      CategoryRepository categoryRepository,
                                      LoyaltyGiftRepository loyaltyGiftRepository,
                                      ObjectMapper objectMapper,
                                      TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingService = embeddingService;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.loyaltyGiftRepository = loyaltyGiftRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public int reindexAll() {
        List<ChunkPayload> chunks = prepareAllChunks();
        return transactionTemplate.execute(status -> {
            jdbcTemplate.execute("CREATE TEMP TABLE rag_chunks_stage (LIKE rag_chunks INCLUDING ALL) ON COMMIT DROP");
            for (ChunkPayload chunk : chunks) {
                insertChunk("rag_chunks_stage", chunk);
            }
            jdbcTemplate.update("DELETE FROM rag_chunks");
            jdbcTemplate.update("""
                    INSERT INTO rag_chunks(doc_id, chunk_index, source_type, lang, title, content, category, valid_until, embedding, updated_at)
                    SELECT doc_id, chunk_index, source_type, lang, title, content, category, valid_until, embedding, updated_at
                    FROM rag_chunks_stage
                    """);
            return chunks.size();
        });
    }

    private List<ChunkPayload> prepareAllChunks() {
        List<ChunkPayload> chunks = new ArrayList<>();
        chunks.addAll(indexProducts());
        chunks.addAll(indexCategories());
        chunks.addAll(indexLoyaltyGifts());
        chunks.addAll(indexRecipesFromMealDb());
        chunks.addAll(indexRecipesFromSpoonacular());
        return chunks;
    }

    private List<ChunkPayload> indexProducts() {
        List<ChunkPayload> chunks = new ArrayList<>();
        for (var p : productRepository.findAllWithCategoryForRag()) {
            String content = "Produit: " + nullSafe(p.getName()) + ". Description: " + nullSafe(p.getDescription())
                    + ". Prix: " + p.getPrice() + ". Stock: " + p.getStockQty();
            chunks.add(buildChunk("product-" + p.getId(), 0, "product", "fr", p.getName(), content,
                    p.getCategory() == null ? null : p.getCategory().getName()));
        }
        return chunks;
    }

    private List<ChunkPayload> indexCategories() {
        List<ChunkPayload> chunks = new ArrayList<>();
        for (var c : categoryRepository.findAll()) {
            String content = "Categorie: " + nullSafe(c.getName()) + ". Affichage: " + nullSafe(c.getDisplayName())
                    + ". Tags: " + nullSafe(c.getCustomTags());
            chunks.add(buildChunk("category-" + c.getId(), 0, "category", "fr", c.getDisplayName(), content, c.getName()));
        }
        return chunks;
    }

    private List<ChunkPayload> indexLoyaltyGifts() {
        List<ChunkPayload> chunks = new ArrayList<>();
        for (var g : loyaltyGiftRepository.findAll()) {
            String content = "Cadeau fidelite: " + nullSafe(g.getTitle()) + ". Points requis: " + g.getPoints();
            chunks.add(buildChunk("loyalty-gift-" + g.getId(), 0, "loyalty", "fr", g.getTitle(), content, "loyalty"));
        }
        return chunks;
    }

    private List<ChunkPayload> indexRecipesFromMealDb() {
        List<ChunkPayload> chunks = new ArrayList<>();
        int indexedRecipes = 0;
        for (char letter = 'a'; letter <= 'z'; letter++) {
            if (indexedRecipes >= MAX_RECIPE_DOCS) {
                break;
            }
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(MEALDB_BY_LETTER + letter))
                        .timeout(Duration.ofSeconds(8))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    continue;
                }
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode meals = root.path("meals");
                if (!meals.isArray()) {
                    continue;
                }
                for (JsonNode meal : meals) {
                    if (indexedRecipes >= MAX_RECIPE_DOCS) {
                        break;
                    }
                    String idMeal = meal.path("idMeal").asText("");
                    String name = meal.path("strMeal").asText("");
                    if (idMeal.isBlank() || name.isBlank()) {
                        continue;
                    }
                    String ingredients = extractIngredients(meal);
                    String instructions = meal.path("strInstructions").asText("");
                    String category = meal.path("strCategory").asText("recipe");
                    String area = meal.path("strArea").asText("");
                    String content = "Recipe: " + name
                            + ". Category: " + category
                            + ". Area: " + area
                            + ". Ingredients: " + ingredients
                            + ". Instructions: " + instructions;

                    chunks.add(buildChunk("recipe-" + idMeal, 0, "recipe", "fr", name, content, category));
                    indexedRecipes++;
                }
            } catch (Exception e) {
                log.debug("MealDB fetch failed for letter {}", letter, e);
            }
        }
        return chunks;
    }

    private List<ChunkPayload> indexRecipesFromSpoonacular() {
        String apiKey = System.getenv("SPOONACULAR_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return List.of();
        }

        List<ChunkPayload> chunks = new ArrayList<>();
        int offset = 0;
        int indexed = 0;
        while (indexed < MAX_SPOON_RECIPES) {
            try {
                String url = SPOON_COMPLEX_SEARCH + "&offset=" + offset + "&apiKey=" + apiKey;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    log.debug("Spoonacular search failed: status={}", response.statusCode());
                    break;
                }

                JsonNode root = objectMapper.readTree(response.body());
                JsonNode results = root.path("results");
                if (!results.isArray() || results.isEmpty()) {
                    break;
                }

                int inPage = 0;
                for (JsonNode recipe : results) {
                    if (indexed >= MAX_SPOON_RECIPES) {
                        break;
                    }
                    String id = recipe.path("id").asText("");
                    String title = recipe.path("title").asText("");
                    if (id.isBlank() || title.isBlank()) {
                        continue;
                    }
                    String ingredients = extractSpoonacularIngredients(recipe);
                    String instructions = recipe.path("instructions").asText("");
                    String content = "Recipe: " + title
                            + ". Ingredients: " + ingredients
                            + ". Instructions: " + instructions;

                    chunks.add(buildChunk("recipe-spoon-" + id, 0, "recipe", "fr", title, content, "recipe"));
                    indexed++;
                    inPage++;
                }

                if (inPage == 0) {
                    break;
                }
                offset += 50;
            } catch (Exception e) {
                log.debug("Spoonacular fetch failed at offset {}", offset, e);
                break;
            }
        }
        return chunks;
    }

    private String extractIngredients(JsonNode meal) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            String ingredient = meal.path("strIngredient" + i).asText("");
            String measure = meal.path("strMeasure" + i).asText("");
            if (ingredient == null || ingredient.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            String ing = ingredient.trim();
            String mea = measure == null ? "" : measure.trim();
            if (!mea.isBlank()) {
                sb.append(mea).append(" ").append(ing);
            } else {
                sb.append(ing);
            }
        }
        return sb.toString();
    }

    private String extractSpoonacularIngredients(JsonNode recipe) {
        StringBuilder sb = new StringBuilder();
        JsonNode ingredients = recipe.path("extendedIngredients");
        if (!ingredients.isArray()) {
            return "";
        }
        for (JsonNode ing : ingredients) {
            String original = ing.path("original").asText("");
            if (original == null || original.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(original.trim());
        }
        return sb.toString();
    }

    private ChunkPayload buildChunk(String docId,
                                    int chunkIndex,
                                    String sourceType,
                                    String lang,
                                    String title,
                                    String content,
                                    String category) {
        String embedding = embeddingService.toPgVectorLiteral(embeddingService.embed(content));
        Timestamp updatedAt = Timestamp.from(Instant.now());
        return new ChunkPayload(docId, chunkIndex, sourceType, lang, title, content, category, embedding, updatedAt);
    }

    private int insertChunk(String targetTable, ChunkPayload chunk) {
        String sql = "INSERT INTO " + targetTable + "(doc_id, chunk_index, source_type, lang, title, content, category, valid_until, embedding, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NULL, CAST(? AS public.vector), ?)";
        return jdbcTemplate.update(sql, chunk.docId, chunk.chunkIndex, chunk.sourceType, chunk.lang,
                chunk.title, chunk.content, chunk.category, chunk.embedding, chunk.updatedAt);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private static class ChunkPayload {
        final String docId;
        final int chunkIndex;
        final String sourceType;
        final String lang;
        final String title;
        final String content;
        final String category;
        final String embedding;
        final Timestamp updatedAt;

        ChunkPayload(String docId, int chunkIndex, String sourceType, String lang, String title,
                     String content, String category, String embedding, Timestamp updatedAt) {
            this.docId = docId;
            this.chunkIndex = chunkIndex;
            this.sourceType = sourceType;
            this.lang = lang;
            this.title = title;
            this.content = content;
            this.category = category;
            this.embedding = embedding;
            this.updatedAt = updatedAt;
        }
    }
}
