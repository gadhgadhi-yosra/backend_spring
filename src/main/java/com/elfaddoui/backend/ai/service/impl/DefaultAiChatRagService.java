package com.elfaddoui.backend.ai.service.impl;

import com.elfaddoui.backend.ai.config.RagProperties;
import com.elfaddoui.backend.ai.dto.AiChatRequest;
import com.elfaddoui.backend.ai.dto.AiChatResponse;
import com.elfaddoui.backend.ai.entity.RagChunk;
import com.elfaddoui.backend.ai.repository.RagChunkRepository;
import com.elfaddoui.backend.ai.service.AiChatRagService;
import com.elfaddoui.backend.ai.service.AiPromptBuilder;
import com.elfaddoui.backend.ai.service.LlmChatService;
import com.elfaddoui.backend.ai.service.RagRetrievalService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DefaultAiChatRagService implements AiChatRagService {

    private final RagRetrievalService ragRetrievalService;
    private final AiPromptBuilder aiPromptBuilder;
    private final LlmChatService llmChatService;
    private final RagChunkRepository ragChunkRepository;
    private final RagProperties ragProperties;
    private final ObjectMapper objectMapper;

    public DefaultAiChatRagService(RagRetrievalService ragRetrievalService,
                                   AiPromptBuilder aiPromptBuilder,
                                   LlmChatService llmChatService,
                                   RagChunkRepository ragChunkRepository,
                                   RagProperties ragProperties,
                                   ObjectMapper objectMapper) {
        this.ragRetrievalService = ragRetrievalService;
        this.aiPromptBuilder = aiPromptBuilder;
        this.llmChatService = llmChatService;
        this.ragChunkRepository = ragChunkRepository;
        this.ragProperties = ragProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        List<RagChunk> chunks = ragRetrievalService.retrieve(request.getMessage(), request.getLocale(), ragProperties.getTopK());
        try {
            String system = aiPromptBuilder.systemPrompt(request.getLocale());
            String user = aiPromptBuilder.userPrompt(request, chunks);
            String json = llmChatService.chatJson(system, user);
            AiChatResponse response = parseResponse(json);
            // If the underlying LLM service returned its deterministic generic fallback,
            // replace it with a chunk-based local answer for better usefulness.
            if (isGenericReply(response.getReply())) {
                response.setReply(localizedFallback(request.getLocale(), request.getMessage(), chunks));
                double fallbackConfidence = estimateFallbackConfidence(request.getMessage(), chunks);
                response.setConfidence(Math.max(0.30, Math.min(response.getConfidence(), fallbackConfidence)));
            }
            response.setSources(chunks.stream().map(RagChunk::getDocId).distinct().limit(8).toList());
            return response;
        } catch (Exception ignored) {
            return fallback(request.getLocale(), request.getMessage(), chunks);
        }
    }

    private AiChatResponse parseResponse(String json) throws Exception {
        JsonNode node = objectMapper.readTree(extractJson(json));
        AiChatResponse response = new AiChatResponse();
        response.setReply(node.path("reply").asText("Je n'ai pas assez d'info exacte pour répondre maintenant."));
        response.setConfidence(node.path("confidence").asDouble(0.35));

        List<AiChatResponse.Action> actions = new ArrayList<>();
        for (JsonNode a : node.path("actions")) {
            actions.add(new AiChatResponse.Action(a.path("type").asText(), a.path("value").asText()));
        }
        response.setActions(actions);
        return response;
    }

    private String extractJson(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstBrace = trimmed.indexOf('{');
            int lastBrace = trimmed.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1);
            }
        }
        return trimmed;
    }

    private AiChatResponse fallback(String locale, String message, List<RagChunk> chunks) {
        AiChatResponse response = new AiChatResponse();
        response.setReply(localizedFallback(locale, message, chunks));
        response.setConfidence(0.30);
        response.setActions(List.of(new AiChatResponse.Action("open_category", "catalogue")));
        response.setSources(chunks.stream().map(RagChunk::getDocId).distinct().limit(8).toList());
        return response;
    }

    private String localizedFallback(String locale, String message, List<RagChunk> chunks) {
        String m = message == null ? "" : message.toLowerCase();
        if (isIngredientIntent(m)) {
            String dishQuery = extractDishQuery(m);
            List<String> dynamicIngredients = extractDynamicIngredients(chunks, dishQuery, locale);
            List<String> catalogProducts = mapIngredientsToCatalogProducts(dynamicIngredients, chunks);
            if ("ar".equalsIgnoreCase(locale)) {
                if (!dynamicIngredients.isEmpty() || !catalogProducts.isEmpty()) {
                    return "مكوّنات مقترحة لـ " + dishQuery + ": "
                            + (dynamicIngredients.isEmpty() ? "غير متوفرة" : String.join("، ", dynamicIngredients))
                            + ". منتجات المتجر المطابقة: "
                            + (catalogProducts.isEmpty() ? "غير متوفرة حالياً" : String.join("، ", catalogProducts))
                            + ".";
                }
                return "ما لقيتش وصفة مطابقة لـ \"" + dishQuery + "\" في البيانات الحالية.";
            }
            if ("en".equalsIgnoreCase(locale)) {
                if (!dynamicIngredients.isEmpty() || !catalogProducts.isEmpty()) {
                    return "Suggested ingredients for " + dishQuery + ": "
                            + (dynamicIngredients.isEmpty() ? "not available" : String.join(", ", dynamicIngredients))
                            + ". Store products found (French labels): "
                            + (catalogProducts.isEmpty() ? "none right now" : String.join(", ", catalogProducts))
                            + ".";
                }
                return "I couldn't find a matching recipe for \"" + dishQuery + "\" in current data.";
            }
            if (!dynamicIngredients.isEmpty() || !catalogProducts.isEmpty()) {
                String label = dishQuery.isBlank() ? "ce plat" : dishQuery;
                return "Ingrédients suggérés pour " + label + ": "
                        + (dynamicIngredients.isEmpty() ? "non disponibles" : String.join(", ", dynamicIngredients))
                        + ". Produits trouvés dans le catalogue: "
                        + (catalogProducts.isEmpty() ? "aucun pour le moment" : String.join(", ", catalogProducts))
                        + ".";
            }
            return dishQuery.isBlank()
                    ? "Je n’ai pas encore de base recettes complète."
                    : "Je n’ai pas trouvé de recette qui correspond à \"" + dishQuery + "\" dans les données actuelles.";
        }

        List<String> picks = chunks.stream()
                .map(RagChunk::getTitle)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .limit(5)
                .toList();

        if (!picks.isEmpty()) {
            String joined = String.join(", ", picks);
            if ("en".equalsIgnoreCase(locale)) {
                return "Here are options from the catalog: " + joined + ".";
            }
            if ("ar".equalsIgnoreCase(locale)) {
                return "إليك خيارات من الكتالوج: " + joined + ".";
            }
            return "Voici des options du catalogue: " + joined + ".";
        }

        if ("en".equalsIgnoreCase(locale)) {
            return "I don't have enough exact data to answer right now.";
        }
        if ("ar".equalsIgnoreCase(locale)) {
            return "لا أملك بيانات دقيقة كافية للإجابة الآن.";
        }
        return "Je n'ai pas assez d'info exacte pour répondre maintenant.";
    }

    private boolean isIngredientIntent(String message) {
        return message.contains("ingredient")
                || message.contains("ingrédient")
                || message.contains("ingredients")
                || message.contains("recette")
                || message.contains("n9adder")
                || message.contains("natyeb")
                || message.contains("ntayeb")
                || message.contains("cuisiner")
                || message.contains("cook")
                || message.contains("makla")
                || message.contains("plat")
                || message.contains("شنو")
                || message.contains("المكونات")
                || message.contains("مكونات")
                || message.contains("متاع")
                || message.contains("كسكسي")
                || message.contains("الكسكسي");
    }

    private boolean isGenericReply(String reply) {
        if (reply == null) return true;
        String r = reply.trim();
        return "Je n'ai pas assez d'info exacte pour répondre maintenant.".equals(r)
                || "I don't have enough exact data to answer right now.".equals(r)
                || "لا أملك بيانات دقيقة كافية للإجابة الآن.".equals(r);
    }

    private List<String> extractDynamicIngredients(List<RagChunk> chunks, String dishQuery, String locale) {
        Set<String> picks = new LinkedHashSet<>();
        List<String> dishTokens = tokenizeDishQuery(dishQuery);
        // 1) Prefer exact lexical recipe matches first.
        if (!dishQuery.isBlank()) {
            List<RagChunk> lexical = ragChunkRepository.searchRecipesByKeyword(dishQuery, 60);
            collectMatchingRecipeIngredients(lexical, dishTokens, picks);
        }

        // 2) If not enough, run a targeted vector retrieval using the dish query.
        if (picks.isEmpty() && !dishTokens.isEmpty()) {
            List<RagChunk> targeted = ragRetrievalService.retrieve(dishQuery, locale, Math.max(ragProperties.getTopK() * 5, 40));
            collectMatchingRecipeIngredients(targeted, dishTokens, picks);
        }

        if (!picks.isEmpty()) {
            return new ArrayList<>(picks);
        }

        // 3) Last fallback: use current product chunks only when no recipe matched.
        for (RagChunk chunk : chunks) {
            if (!"product".equalsIgnoreCase(chunk.getSourceType())) {
                continue;
            }
            String title = chunk.getTitle();
            if (title == null || title.isBlank()) {
                continue;
            }
            String cleaned = title.trim();
            String low = cleaned.toLowerCase(Locale.ROOT);
            // Exclude obvious non-food/noise names.
            if (low.contains("box") || low.contains("sac") || low.matches(".*\\bb+\\b.*")) {
                continue;
            }
            picks.add(cleaned);
            if (picks.size() >= 8) {
                break;
            }
        }
        return new ArrayList<>(picks);
    }

    private void collectMatchingRecipeIngredients(List<RagChunk> chunks,
                                                  List<String> dishTokens,
                                                  Set<String> picks) {
        for (RagChunk chunk : chunks) {
            if (!"recipe".equalsIgnoreCase(chunk.getSourceType())) {
                continue;
            }
            String haystack = ((chunk.getTitle() == null ? "" : chunk.getTitle()) + " "
                    + (chunk.getContent() == null ? "" : chunk.getContent())).toLowerCase(Locale.ROOT);
            if (!dishTokens.isEmpty() && !matchesDishTokens(haystack, dishTokens)) {
                continue;
            }
            String content = chunk.getContent() == null ? "" : chunk.getContent();
            int idx = content.toLowerCase(Locale.ROOT).indexOf("ingredients:");
            if (idx >= 0) {
                String tail = content.substring(idx + "ingredients:".length());
                int end = tail.toLowerCase(Locale.ROOT).indexOf(". instructions:");
                String ingredientPart = end >= 0 ? tail.substring(0, end) : tail;
                for (String piece : ingredientPart.split(",")) {
                    String cleaned = simplifyIngredient(piece);
                    if (!cleaned.isBlank() && cleaned.length() >= 2 && cleaned.length() <= 60) {
                        picks.add(cleaned);
                    }
                    if (picks.size() >= 10) {
                        return;
                    }
                }
            }
        }
    }

    private String extractDishQuery(String message) {
        if (message == null || message.isBlank()) return "";
        String m = message.replace("?", " ").replace("؟", " ").trim();
        String low = m.toLowerCase(Locale.ROOT);

        String[] markers = {
                "ingredients of ", "ingredients for ", "of ", "for ",
                "ingrédients de ", "ingrédients du ", "ingrédients pour ",
                "de ", "du ", "pour ",
                "متاع ", "تاع ", "لـ ", "ل "
        };
        for (String marker : markers) {
            int idx = low.indexOf(marker);
            if (idx >= 0) {
                String tail = m.substring(Math.min(m.length(), idx + marker.length())).trim();
                if (!tail.isBlank()) return tail;
            }
        }
        return normalizeDishName(low);
    }

    private List<String> tokenizeDishQuery(String dishQuery) {
        if (dishQuery == null || dishQuery.isBlank()) return List.of();
        Set<String> stop = Set.of(
                "ingredient", "ingredients", "ingrédient", "ingrédients",
                "what", "which", "are", "the", "for", "of",
                "c", "quoi", "les", "de", "du", "des", "la", "le", "l",
                "recette", "pour", "avec", "chnowa", "makla", "plat", "cuisiner", "cook",
                "شنو", "ما", "هي", "المكونات", "متاع", "تاع"
        );
        List<String> baseTokens = Arrays.stream(dishQuery.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{Nd}]+"))
                .map(String::trim)
                .filter(s -> s.length() >= 3)
                .filter(s -> !stop.contains(s))
                .distinct()
                .collect(Collectors.toList());

        Set<String> expanded = new LinkedHashSet<>(baseTokens);
        if (expanded.contains("couscous")) {
            expanded.add("cous cous");
            expanded.add("cuscus");
            expanded.add("كسكسي");
            expanded.add("الكسكسي");
        }
        if (expanded.contains("ma9rouna") || expanded.contains("makrouna")) {
            expanded.add("pasta");
            expanded.add("spaghetti");
            expanded.add("macaroni");
        }
        return new ArrayList<>(expanded);
    }

    private boolean matchesDishTokens(String haystack, List<String> dishTokens) {
        if (dishTokens.isEmpty()) return false;
        int longHits = 0;
        for (String token : dishTokens) {
            if (token.length() >= 5 && haystack.contains(token)) {
                longHits++;
            }
        }
        return longHits > 0;
    }

    private String simplifyIngredient(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT).trim();
        // remove most quantities/units
        s = s.replaceAll("[0-9¼½¾⅓⅔/\\.\\-]+", " ");
        s = s.replaceAll("\\b(tsp|tbsp|teaspoon|tablespoon|cup|cups|ml|l|oz|ounce|ounces|g|kg|gr|lb|lbs|pinch|dash|clove|cloves)\\b", " ");
        s = s.replaceAll("\\b(chopped|minced|diced|fresh|ground|optional|to taste|rinsed|drained|cooked)\\b", " ");
        s = s.replaceAll("\\b(and|or|with|plus)\\b", " ");
        s = s.replaceAll("[^\\p{L}\\s]", " ");
        s = s.replaceAll("\\s+", " ").trim();
        if (s.isBlank()) return "";
        if (s.split("\\s+").length > 4) return "";
        if (s.length() < 3) return "";

        Map<String, String> map = new HashMap<>();
        map.put("couscous", "couscous");
        map.put("olive oil", "huile d'olive");
        map.put("oil", "huile");
        map.put("tomato paste", "concentré tomate");
        map.put("tomato sauce", "sauce tomate");
        map.put("tomato", "tomate");
        map.put("onion", "oignon");
        map.put("garlic", "ail");
        map.put("carrot", "carotte");
        map.put("zucchini", "courgette");
        map.put("potato", "pomme de terre");
        map.put("chickpea", "pois chiches");
        map.put("chicken", "poulet");
        map.put("beef", "viande");
        map.put("lamb", "agneau");
        map.put("salt", "sel");
        map.put("pepper", "poivre");
        map.put("cumin", "cumin");
        map.put("paprika", "paprika");
        map.put("turmeric", "curcuma");
        map.put("coriander", "coriandre");
        map.put("parsley", "persil");
        map.put("harissa", "harissa");
        map.put("lemon", "citron");
        map.put("butter", "beurre");

        for (Map.Entry<String, String> e : map.entrySet()) {
            if (s.contains(e.getKey())) {
                return e.getValue();
            }
        }
        // default: short cleaned token
        if (s.length() > 24) {
            return "";
        }
        return s;
    }

    private String normalizeDishName(String value) {
        if (value == null) return "";
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("كسكسي") || v.contains("الكسكسي")) return "couscous";
        if (v.contains("ma9rouna") || v.contains("makrouna")) return "ma9rouna";
        return v;
    }

    private List<String> mapIngredientsToCatalogProducts(List<String> ingredients, List<RagChunk> initialChunks) {
        Map<String, String> uniqueProducts = new LinkedHashMap<>();

        // 1) map using current retrieved product chunks
        for (String ing : ingredients) {
            String token = ing.toLowerCase(Locale.ROOT);
            for (RagChunk c : initialChunks) {
                if (!"product".equalsIgnoreCase(c.getSourceType()) || c.getTitle() == null) {
                    continue;
                }
                String title = c.getTitle().trim();
                String h = (c.getTitle() + " " + (c.getContent() == null ? "" : c.getContent())).toLowerCase(Locale.ROOT);
                if (h.contains(token) || token.contains(h)) {
                    String key = canonicalKey(title);
                    uniqueProducts.putIfAbsent(key, title);
                }
                if (uniqueProducts.size() >= 8) {
                    return new ArrayList<>(uniqueProducts.values());
                }
            }
        }

        // 2) lexical repository fallback to find local products for each ingredient
        for (String ing : ingredients) {
            List<RagChunk> found = ragChunkRepository.searchProductsByKeyword(ing, 5);
            for (RagChunk c : found) {
                if (c.getTitle() != null && !c.getTitle().isBlank()) {
                    String title = c.getTitle().trim();
                    String key = canonicalKey(title);
                    uniqueProducts.putIfAbsent(key, title);
                }
                if (uniqueProducts.size() >= 8) {
                    return new ArrayList<>(uniqueProducts.values());
                }
            }
        }
        return new ArrayList<>(uniqueProducts.values());
    }

    private String canonicalKey(String value) {
        if (value == null) return "";
        String v = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{Nd}\\s]", " ")
                .replaceAll("\\b\\d+(kg|g|l|ml)?\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return v;
    }

    private double estimateFallbackConfidence(String message, List<RagChunk> chunks) {
        String m = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (!isIngredientIntent(m)) return 0.35;

        String dishQuery = extractDishQuery(m);
        List<String> ingredients = extractDynamicIngredients(chunks, dishQuery, "fr");
        List<String> products = mapIngredientsToCatalogProducts(ingredients, chunks);
        return computeFallbackConfidence(ingredients, products);
    }

    private double computeFallbackConfidence(List<String> ingredients, List<String> products) {
        if (ingredients.size() >= 6 && products.size() >= 4) return 0.58;
        if (ingredients.size() >= 4 && products.size() >= 3) return 0.55;
        if (ingredients.size() >= 2 || products.size() >= 2) return 0.45;
        return 0.32;
    }
}
