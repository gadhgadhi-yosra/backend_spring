package com.elfaddoui.backend.ai.service.impl;

import com.elfaddoui.backend.ai.dto.AiChatResponse;
import com.elfaddoui.backend.ai.dto.BudgetAssistantRequest;
import com.elfaddoui.backend.ai.dto.BudgetAssistantResponse;
import com.elfaddoui.backend.ai.entity.RagChunk;
import com.elfaddoui.backend.ai.repository.RagChunkRepository;
import com.elfaddoui.backend.ai.service.BudgetAssistantService;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DefaultBudgetAssistantService implements BudgetAssistantService {
    private final ProductRepository productRepository;
    private final RagChunkRepository ragChunkRepository;

    public DefaultBudgetAssistantService(ProductRepository productRepository,
                                         RagChunkRepository ragChunkRepository) {
        this.productRepository = productRepository;
        this.ragChunkRepository = ragChunkRepository;
    }

    @Override
    public BudgetAssistantResponse buildPlan(BudgetAssistantRequest request) {
        double budget = request.getBudget() == null ? 50.0 : request.getBudget();
        String locale = normalizeLocale(request.getLocale());
        String message = request.getMessage() == null ? "" : request.getMessage().toLowerCase(Locale.ROOT);

        List<Product> catalog = productRepository.findAll().stream()
                .filter(Product::isActive)
                .filter(p -> p.getStockQty() != null && p.getStockQty() > 0)
                .filter(p -> p.getPrice() != null && p.getPrice().doubleValue() > 0)
                .toList();

        Set<String> wantedKeywords = inferKeywords(message);
        Set<String> recipeIngredients = inferRecipeIngredients(message);
        wantedKeywords.addAll(recipeIngredients);

        List<Product> preferred = wantedKeywords.isEmpty()
                ? catalog
                : catalog.stream().filter(p -> matchesAnyKeyword(p, wantedKeywords)).toList();

        List<Product> pool = preferred.isEmpty() ? catalog : preferred;
        List<Product> sorted = pool.stream()
                .sorted(Comparator
                        .comparing((Product p) -> p.getPrice().doubleValue())
                        .thenComparing((Product p) -> p.getDiscountPct() == null ? 0 : -p.getDiscountPct())
                        .thenComparing((Product p) -> p.getRating() == null ? 0.0 : -p.getRating()))
                .toList();

        List<Product> chosen = buildDiversifiedPlan(sorted, budget, 10);
        double total = chosen.stream().mapToDouble(p -> p.getPrice().doubleValue()).sum();

        double savings = chosen.stream()
                .mapToDouble(this::savingFor)
                .sum();

        BudgetAssistantResponse response = new BudgetAssistantResponse();
        response.setBudget(round2(budget));
        response.setEstimatedTotal(round2(total));
        response.setEstimatedSavings(round2(savings));
        response.setItems(chosen.stream().map(this::toItem).toList());
        response.setSubstitutions(buildSubstitutions(catalog, wantedKeywords, chosen));
        response.setActions(List.of(
                new AiChatResponse.Action("open_category", "catalogue"),
                new AiChatResponse.Action("apply_budget_plan", "true")
        ));
        response.setMessage(buildMessage(locale, budget, total, chosen.size()));
        return response;
    }

    private BudgetAssistantResponse.Item toItem(Product p) {
        BudgetAssistantResponse.Item item = new BudgetAssistantResponse.Item();
        item.setProductId(p.getId());
        item.setName(p.getName());
        item.setImageUrl(p.getImageUrl());
        item.setPrice(round2(p.getPrice().doubleValue()));
        item.setCategory(resolveCategoryFromRag(p));
        item.setReason(reasonFor(p));
        return item;
    }

    private List<BudgetAssistantResponse.Substitution> buildSubstitutions(List<Product> catalog,
                                                                          Set<String> wantedKeywords,
                                                                          List<Product> chosen) {
        List<BudgetAssistantResponse.Substitution> out = new ArrayList<>();
        if (wantedKeywords.isEmpty()) {
            return out;
        }
        Set<Long> chosenIds = chosen.stream().map(Product::getId).collect(HashSet::new, HashSet::add, HashSet::addAll);
        for (String kw : wantedKeywords) {
            Product alt = catalog.stream()
                    .filter(p -> !chosenIds.contains(p.getId()))
                    .filter(p -> matchesKeyword(p, kw))
                    .min(Comparator.comparing(p -> p.getPrice().doubleValue()))
                    .orElse(null);
            if (alt == null) continue;
            BudgetAssistantResponse.Substitution s = new BudgetAssistantResponse.Substitution();
            s.setOriginalKeyword(kw);
            s.setSuggestedProductId(alt.getId());
            s.setSuggestedName(alt.getName());
            s.setPrice(round2(alt.getPrice().doubleValue()));
            s.setReason("Alternative moins chère dans le catalogue");
            out.add(s);
            if (out.size() >= 5) break;
        }
        return out;
    }

    private List<Product> buildDiversifiedPlan(List<Product> sortedPool, double budget, int maxItems) {
        List<Product> chosen = new ArrayList<>();
        Set<Long> chosenIds = new HashSet<>();
        Set<String> chosenCategories = new HashSet<>();
        double total = 0.0;

        // Pass 1: diversify by category (cheapest in each category first)
        Map<String, Product> cheapestByCategory = new LinkedHashMap<>();
        for (Product p : sortedPool) {
            String cat = resolveCategoryFromRag(p);
            if (cat.isBlank()) cat = "autres";
            cheapestByCategory.putIfAbsent(cat, p);
        }
        for (Product p : cheapestByCategory.values()) {
            if (chosen.size() >= maxItems) break;
            double price = p.getPrice().doubleValue();
            String cat = resolveCategoryFromRag(p);
            if (total + price <= budget || chosen.isEmpty()) {
                chosen.add(p);
                chosenIds.add(p.getId());
                chosenCategories.add(cat);
                total += price;
            }
        }

        // Pass 2: fill remaining budget with best cheap options
        for (Product p : sortedPool) {
            if (chosen.size() >= maxItems) break;
            if (p.getId() == null || chosenIds.contains(p.getId())) continue;
            double price = p.getPrice().doubleValue();
            if (total + price <= budget) {
                chosen.add(p);
                chosenIds.add(p.getId());
                total += price;
            }
        }
        return chosen;
    }

    private String reasonFor(Product p) {
        if (p.getDiscountPct() != null && p.getDiscountPct() > 0) {
            return "Promo " + p.getDiscountPct() + "%";
        }
        if (p.getRating() != null && p.getRating() >= 4.5) {
            return "Très bien noté";
        }
        return "Prix adapté au budget";
    }

    private double savingFor(Product p) {
        BigDecimal oldPrice = p.getOldPrice();
        BigDecimal price = p.getPrice();
        if (oldPrice == null || price == null || oldPrice.compareTo(price) <= 0) {
            return 0.0;
        }
        return oldPrice.subtract(price).doubleValue();
    }

    private String buildMessage(String locale, double budget, double total, int count) {
        if ("ar".equals(locale)) {
            return "اقترحت " + count + " منتج بميزانية " + round2(budget) + " د.ت (الإجمالي: " + round2(total) + " د.ت).";
        }
        if ("en".equals(locale)) {
            return "I selected " + count + " products for a budget of " + round2(budget) + " DT (total: " + round2(total) + " DT).";
        }
        return "J’ai sélectionné " + count + " produits pour un budget de " + round2(budget) + " DT (total: " + round2(total) + " DT).";
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) return "fr";
        String l = locale.trim().toLowerCase(Locale.ROOT);
        return switch (l) {
            case "en", "ar", "fr" -> l;
            default -> "fr";
        };
    }

    private Set<String> inferKeywords(String message) {
        Set<String> tokens = new HashSet<>();
        if (message == null || message.isBlank()) return tokens;
        for (String t : message.split("[^\\p{L}\\p{Nd}]+")) {
            String s = t.trim().toLowerCase(Locale.ROOT);
            if (s.length() >= 3 && !Set.of("budget", "dt", "panier", "pour", "avec", "les", "des", "de", "la", "le").contains(s)) {
                tokens.add(s);
            }
        }
        return tokens;
    }

    private Set<String> inferRecipeIngredients(String message) {
        Set<String> tokens = new LinkedHashSet<>();
        if (message == null || message.isBlank()) {
            return tokens;
        }
        String dish = extractDishQuery(message);
        if (dish.isBlank()) {
            return tokens;
        }
        List<RagChunk> recipes = ragChunkRepository.searchRecipesByKeyword(dish, 20);
        for (RagChunk r : recipes) {
            String content = r.getContent() == null ? "" : r.getContent();
            int idx = content.toLowerCase(Locale.ROOT).indexOf("ingredients:");
            if (idx < 0) continue;
            String tail = content.substring(idx + "ingredients:".length());
            int end = tail.toLowerCase(Locale.ROOT).indexOf(". instructions:");
            String ingredientPart = end >= 0 ? tail.substring(0, end) : tail;
            for (String piece : ingredientPart.split(",")) {
                String kw = simplifyToKeyword(piece);
                if (!kw.isBlank()) tokens.add(kw);
                if (tokens.size() >= 30) return tokens;
            }
        }
        return tokens;
    }

    private boolean matchesAnyKeyword(Product p, Set<String> keywords) {
        for (String k : keywords) {
            if (matchesKeyword(p, k)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesKeyword(Product p, String keyword) {
        String hay = (nvl(p.getName()) + " " + nvl(p.getDescription()) + " "
                + nvl(p.getCustomTags()))
                .toLowerCase(Locale.ROOT);
        return hay.contains(keyword);
    }

    private String resolveCategoryFromRag(Product p) {
        if (p.getId() == null) return "";
        List<RagChunk> found = ragChunkRepository.findByDocId("product-" + p.getId());
        if (!found.isEmpty()) {
            String cat = found.get(0).getCategory();
            return cat == null ? "" : cat;
        }
        return "";
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String extractDishQuery(String message) {
        String m = message == null ? "" : message.replace("?", " ").trim().toLowerCase(Locale.ROOT);
        int idx = m.indexOf(" de ");
        if (idx >= 0 && idx + 4 < m.length()) {
            return m.substring(idx + 4).trim();
        }
        List<String> stop = List.of("budget", "panier", "dt", "ingredients", "ingrédients", "recette");
        return Arrays.stream(m.split("[^\\p{L}\\p{Nd}]+"))
                .map(String::trim)
                .filter(s -> s.length() >= 3)
                .filter(s -> !stop.contains(s))
                .collect(Collectors.joining(" "));
    }

    private String simplifyToKeyword(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT).trim();
        s = s.replaceAll("[0-9¼½¾⅓⅔/\\.\\-]+", " ");
        s = s.replaceAll("\\b(tsp|tbsp|teaspoon|tablespoon|cup|cups|ml|l|oz|ounce|ounces|g|kg|gr|lb|lbs|pinch|dash|clove|cloves)\\b", " ");
        s = s.replaceAll("\\b(chopped|minced|diced|fresh|ground|optional|to taste)\\b", " ");
        s = s.replaceAll("[^\\p{L}\\s]", " ");
        s = s.replaceAll("\\s+", " ").trim();
        if (s.isBlank()) return "";

        if (s.contains("tomato")) return "tomate";
        if (s.contains("onion")) return "oignon";
        if (s.contains("garlic")) return "ail";
        if (s.contains("carrot")) return "carotte";
        if (s.contains("zucchini")) return "courgette";
        if (s.contains("potato")) return "pomme de terre";
        if (s.contains("chickpea")) return "pois chiches";
        if (s.contains("olive oil") || s.equals("oil")) return "huile";
        if (s.contains("couscous")) return "couscous";
        if (s.contains("salt")) return "sel";
        if (s.contains("pepper")) return "poivre";
        if (s.contains("cumin")) return "cumin";
        if (s.contains("paprika")) return "paprika";
        if (s.contains("harissa")) return "harissa";

        String[] parts = s.split("\\s+");
        return parts.length == 0 ? "" : parts[parts.length - 1];
    }

    private double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
