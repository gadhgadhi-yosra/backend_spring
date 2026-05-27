package com.elfaddoui.backend;

import com.elfaddoui.backend.appconfig.entity.AppConfigEntry;
import com.elfaddoui.backend.category.entity.Category;
import com.elfaddoui.backend.order.entity.Order;
import com.elfaddoui.backend.order.entity.DeliverySlot;
import com.elfaddoui.backend.order.entity.OrderItem;
import com.elfaddoui.backend.order.entity.OrderStatus;
import com.elfaddoui.backend.order.entity.PaymentMethod;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeliveryTrackingIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void authenticatedUserCanCancelPendingOrConfirmedOrder() throws Exception {
        String token = clientToken();
        User user = userRepository.findByEmail("client@elfaddoui.test").orElseThrow();
        Category category = createCategory("Annulation");
        Product product = createProduct("Produit annule", category, new BigDecimal("10.00"), 0, 4.5, true);
        product.setStockQty(7);
        product.setSalesCount(5L);
        product = productRepository.save(product);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setReference("ELF-20260425-0100");
        order.setPaymentMethod(PaymentMethod.CASH);
        order.setDeliverySlot(DeliverySlot.ASAP);
        order.setFullName("Client Test");
        order.setPhone("+216 20 123 456");
        order.setEmail("client@elfaddoui.test");
        order.setCity("Tunis");
        order.setArea("Centre");
        order.setStreet("Rue 10");
        order.setPlaceType("Maison");
        order.setSubtotal(new BigDecimal("10.00"));
        order.setDeliveryFee(new BigDecimal("4.00"));
        order.setTotal(new BigDecimal("14.00"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQty(3);
        item.setProductNameSnapshot(product.getName());
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setLineTotal(new BigDecimal("30.00"));
        order.getItems().add(item);

        orderRepository.save(order);

        mockMvc.perform(patch("/api/orders/{orderReference}/cancel", "ELF-20260425-0100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderReference", is("ELF-20260425-0100")))
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.total", is(14.00)))
                .andExpect(jsonPath("$.itemsCount", is(1)));

        Order cancelledOrder = orderRepository.findByReferenceIgnoreCaseAndUserEmail("ELF-20260425-0100", user.getEmail())
                .orElseThrow();
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();

        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(updatedProduct.getStockQty()).isEqualTo(10);
        assertThat(updatedProduct.getSalesCount()).isEqualTo(2L);
    }

    @Test
    void authenticatedUserCannotCancelShippedOrder() throws Exception {
        String token = clientToken();
        User user = userRepository.findByEmail("client@elfaddoui.test").orElseThrow();

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.SHIPPED);
        order.setReference("ELF-20260425-0101");
        order.setPaymentMethod(PaymentMethod.CASH);
        order.setDeliverySlot(DeliverySlot.ASAP);
        order.setFullName("Client Test");
        order.setPhone("+216 20 123 456");
        order.setEmail("client@elfaddoui.test");
        order.setCity("Tunis");
        order.setArea("Centre");
        order.setStreet("Rue 11");
        order.setPlaceType("Maison");
        order.setSubtotal(new BigDecimal("10.00"));
        order.setDeliveryFee(new BigDecimal("4.00"));
        order.setTotal(new BigDecimal("14.00"));
        orderRepository.save(order);

        mockMvc.perform(patch("/api/orders/{orderReference}/cancel", "ELF-20260425-0101")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Order cannot be cancelled anymore")));
    }

    @Test
    void authenticatedUserCanReadOrderHistory() throws Exception {
        String token = clientToken();
        User user = userRepository.findByEmail("client@elfaddoui.test").orElseThrow();
        Category category = createCategory("Historique");
        Product orange = createProduct("Orange historique", category, new BigDecimal("5.00"), 0, 4.5, true);
        Product tomato = createProduct("Tomate historique", category, new BigDecimal("6.00"), 0, 4.3, true);
        Product paprika = createProduct("Paprika historique", category, new BigDecimal("6.00"), 0, 4.1, true);

        Order older = new Order();
        older.setUser(user);
        older.setStatus(OrderStatus.CONFIRMED);
        older.setReference("ELF-20260424-0001");
        older.setPaymentMethod(PaymentMethod.CASH);
        older.setDeliverySlot(DeliverySlot.ASAP);
        older.setFullName("Client Test");
        older.setPhone("+216 20 123 456");
        older.setEmail("client@elfaddoui.test");
        older.setCity("Tunis");
        older.setArea("Centre");
        older.setStreet("Rue 1");
        older.setPlaceType("Maison");
        older.setSubtotal(new BigDecimal("10.00"));
        older.setDeliveryFee(new BigDecimal("4.00"));
        older.setTotal(new BigDecimal("14.00"));
        older.setCourierName("Older Courier");
        older.setCourierPhone("+216 11 111 111");
        older.setStorePhone("+216 71 000 001");

        OrderItem olderItem = new OrderItem();
        olderItem.setOrder(older);
        olderItem.setProduct(orange);
        olderItem.setQty(2);
        olderItem.setProductNameSnapshot("Orange");
        olderItem.setUnitPrice(new BigDecimal("5.00"));
        olderItem.setLineTotal(new BigDecimal("10.00"));
        older.getItems().add(olderItem);
        orderRepository.save(older);

        Order latest = new Order();
        latest.setUser(user);
        latest.setStatus(OrderStatus.SHIPPED);
        latest.setReference("ELF-20260425-0002");
        latest.setPaymentMethod(PaymentMethod.CASH);
        latest.setDeliverySlot(DeliverySlot.SCHEDULED);
        latest.setScheduledTime("18:00 - 19:00");
        latest.setFullName("Client Test");
        latest.setPhone("+216 20 123 456");
        latest.setEmail("client@elfaddoui.test");
        latest.setCity("Tunis");
        latest.setArea("Centre");
        latest.setStreet("Rue 2");
        latest.setPlaceType("Maison");
        latest.setSubtotal(new BigDecimal("12.00"));
        latest.setDeliveryFee(new BigDecimal("5.00"));
        latest.setTotal(new BigDecimal("17.00"));
        latest.setCourierName("Latest Courier");
        latest.setCourierPhone("+216 22 222 222");
        latest.setStorePhone("+216 71 000 002");

        OrderItem latestItem1 = new OrderItem();
        latestItem1.setOrder(latest);
        latestItem1.setProduct(tomato);
        latestItem1.setQty(1);
        latestItem1.setProductNameSnapshot("Tomate");
        latestItem1.setUnitPrice(new BigDecimal("6.00"));
        latestItem1.setLineTotal(new BigDecimal("6.00"));
        latest.getItems().add(latestItem1);

        OrderItem latestItem2 = new OrderItem();
        latestItem2.setOrder(latest);
        latestItem2.setProduct(paprika);
        latestItem2.setQty(1);
        latestItem2.setProductNameSnapshot("Paprika");
        latestItem2.setUnitPrice(new BigDecimal("6.00"));
        latestItem2.setLineTotal(new BigDecimal("6.00"));
        latest.getItems().add(latestItem2);
        orderRepository.save(latest);

        mockMvc.perform(get("/api/orders/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderReference", is("ELF-20260425-0002")))
                .andExpect(jsonPath("$[0].status", is("SHIPPED")))
                .andExpect(jsonPath("$[0].total", is(17.00)))
                .andExpect(jsonPath("$[0].itemsCount", is(2)))
                .andExpect(jsonPath("$[1].orderReference", is("ELF-20260424-0001")))
                .andExpect(jsonPath("$[1].status", is("CONFIRMED")))
                .andExpect(jsonPath("$[1].total", is(14.00)))
                .andExpect(jsonPath("$[1].itemsCount", is(1)));
    }

    @Test
    void authenticatedUserCanReadLatestTracking() throws Exception {
        String token = clientToken();
        User user = userRepository.findByEmail("client@elfaddoui.test").orElseThrow();

        Order older = new Order();
        older.setUser(user);
        older.setStatus(OrderStatus.CONFIRMED);
        older.setReference("ELF-20260424-0001");
        older.setPaymentMethod(PaymentMethod.CASH);
        older.setDeliverySlot(DeliverySlot.ASAP);
        older.setFullName("Client Test");
        older.setPhone("+216 20 123 456");
        older.setEmail("client@elfaddoui.test");
        older.setCity("Tunis");
        older.setArea("Centre");
        older.setStreet("Rue 1");
        older.setPlaceType("Maison");
        older.setSubtotal(new BigDecimal("10.00"));
        older.setDeliveryFee(new BigDecimal("4.00"));
        older.setTotal(new BigDecimal("14.00"));
        older.setCourierName("Older Courier");
        older.setCourierPhone("+216 11 111 111");
        older.setStorePhone("+216 71 000 001");
        orderRepository.save(older);

        Order latest = new Order();
        latest.setUser(user);
        latest.setStatus(OrderStatus.SHIPPED);
        latest.setReference("ELF-20260425-0002");
        latest.setPaymentMethod(PaymentMethod.CASH);
        latest.setDeliverySlot(DeliverySlot.SCHEDULED);
        latest.setScheduledTime("18:00 - 19:00");
        latest.setFullName("Client Test");
        latest.setPhone("+216 20 123 456");
        latest.setEmail("client@elfaddoui.test");
        latest.setCity("Tunis");
        latest.setArea("Centre");
        latest.setStreet("Rue 2");
        latest.setPlaceType("Maison");
        latest.setSubtotal(new BigDecimal("12.00"));
        latest.setDeliveryFee(new BigDecimal("5.00"));
        latest.setTotal(new BigDecimal("17.00"));
        latest.setCourierName("Latest Courier");
        latest.setCourierPhone("+216 22 222 222");
        latest.setStorePhone("+216 71 000 002");
        orderRepository.save(latest);

        mockMvc.perform(get("/api/orders/latest")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode", is("ELF-20260425-0002")))
                .andExpect(jsonPath("$.status", is("on_route")))
                .andExpect(jsonPath("$.deliveryAddress", is("Tunis, Centre, Rue 2")))
                .andExpect(jsonPath("$.deliverySlotLabel", is("18:00 - 19:00")))
                .andExpect(jsonPath("$.deliveryFee", is(5.00)))
                .andExpect(jsonPath("$.courierName", is("Latest Courier")))
                .andExpect(jsonPath("$.courierPhone", is("+216 22 222 222")))
                .andExpect(jsonPath("$.storePhone", is("+216 71 000 002")));
    }

    @Test
    void authenticatedUserCanReadDeliverySettings() throws Exception {
        appConfigRepository.save(new AppConfigEntry("delivery.fee", "6.50"));
        appConfigRepository.save(new AppConfigEntry("delivery.eta.label", "20-30 min"));
        appConfigRepository.save(new AppConfigEntry("delivery.courier.name", "Sami"));
        appConfigRepository.save(new AppConfigEntry("delivery.courier.phone", "+216 55 000 111"));
        appConfigRepository.save(new AppConfigEntry("delivery.store.phone", "+216 71 999 000"));

        mockMvc.perform(get("/api/orders/delivery/settings")
                        .header("Authorization", "Bearer " + clientToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryFee", is(6.50)))
                .andExpect(jsonPath("$.deliveryEtaLabel", is("20-30 min")))
                .andExpect(jsonPath("$.courierName", is("Sami")))
                .andExpect(jsonPath("$.courierPhone", is("+216 55 000 111")))
                .andExpect(jsonPath("$.storePhone", is("+216 71 999 000")));
    }

    @Test
    void authenticatedUserCanCreateOrderAndReadTracking() throws Exception {
        Category category = createCategory("Fruits");
        Product orange = createProduct("Orange", category, new BigDecimal("4.50"), 0, 4.8, true);
        String token = clientToken();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "qty": 2
                                }
                                """.formatted(orange.getId())))
                .andExpect(status().isCreated());

        String createResponse = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("customer", Map.of(
                                        "fullName", "Client Test",
                                        "phone", "+216 20 123 456",
                                        "email", "client@elfaddoui.test",
                                        "note", "Call before arrival"
                                )),
                                Map.entry("address", Map.of(
                                        "city", "Tunis",
                                        "area", "Centre",
                                        "street", "Rue de Marseille",
                                        "extra", "Immeuble A",
                                        "postalCode", "1000",
                                        "hint", "Near the station",
                                        "placeType", "Maison"
                                )),
                                Map.entry("payment", Map.of("method", "cash")),
                                Map.entry("delivery", Map.of(
                                        "slot", "scheduled",
                                        "scheduledTime", "18:00 - 19:00"
                                )),
                                Map.entry("total", 999.99)
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.total", is(13.00)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderCode = objectMapper.readTree(createResponse).get("orderId").asText();
        org.assertj.core.api.Assertions.assertThat(orderCode).startsWith("ELF-20");

        mockMvc.perform(get("/api/orders/{orderCode}/tracking", orderCode)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode", is(orderCode)))
                .andExpect(jsonPath("$.orderId", is(orderCode)))
                .andExpect(jsonPath("$.status", is("preparing")))
                .andExpect(jsonPath("$.step", is(0)))
                .andExpect(jsonPath("$.etaMinutes", is(35)))
                .andExpect(jsonPath("$.deliveryAddress", is("Tunis, Centre, Rue de Marseille, Immeuble A")))
                .andExpect(jsonPath("$.deliverySlotLabel", is("18:00 - 19:00")))
                .andExpect(jsonPath("$.deliveryFee", is(4.00)))
                .andExpect(jsonPath("$.courierName", is("Ali Ben Salem")))
                .andExpect(jsonPath("$.courierPhone", is("+216 55 123 456")))
                .andExpect(jsonPath("$.storePhone", is("+216 71 000 111")));

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems", is(0)));
    }

    @Test
    void checkoutHandlesLegacyNullSalesCount() throws Exception {
        Category category = createCategory("Legumes");
        Product tomato = createProduct("Tomate", category, new BigDecimal("3.00"), 0, 4.2, true);
        tomato.setSalesCount(null);
        productRepository.save(tomato);
        String token = clientToken();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "qty": 1
                                }
                                """.formatted(tomato.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("customer", Map.of(
                                        "fullName", "Client Legacy",
                                        "phone", "+216 20 123 456",
                                        "email", "legacy@elfaddoui.test"
                                )),
                                Map.entry("address", Map.of(
                                        "city", "Tunis",
                                        "area", "Centre",
                                        "street", "Rue test",
                                        "placeType", "Maison"
                                )),
                                Map.entry("payment", Map.of("method", "cash")),
                                Map.entry("delivery", Map.of("slot", "asap")),
                                Map.entry("total", 999.99)
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.total", is(7.00)));
    }

    @Test
    void checkoutValidationErrorIncludesInvalidFieldInMessage() throws Exception {
        Category category = createCategory("Epices");
        Product paprika = createProduct("Paprika", category, new BigDecimal("5.00"), 0, 4.7, true);
        String token = clientToken();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "qty": 1
                                }
                                """.formatted(paprika.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("customer", Map.of(
                                        "fullName", "Client Validation",
                                        "phone", "20123456",
                                        "email", "validation@elfaddoui.test"
                                )),
                                Map.entry("address", Map.of(
                                        "city", "Tunis",
                                        "area", "Centre",
                                        "street", "Rue validation",
                                        "placeType", "Maison"
                                )),
                                Map.entry("payment", Map.of("method", "cash")),
                                Map.entry("delivery", Map.of("slot", "asap")),
                                Map.entry("total", 999.99)
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation error: customer.phone phone must match +216 XX XXX XXX")))
                .andExpect(jsonPath("$.validationErrors['customer.phone']", is("phone must match +216 XX XXX XXX")));
    }

    @Test
    void trackingIsScopedToCurrentUser() throws Exception {
        clientToken("first@elfaddoui.test");
        User firstUser = userRepository.findByEmail("first@elfaddoui.test").orElseThrow();

        Order order = new Order();
        order.setUser(firstUser);
        order.setStatus(OrderStatus.SHIPPED);
        order.setReference("ELF-20260424-0001");
        order.setPaymentMethod(PaymentMethod.CASH);
        order.setDeliverySlot(DeliverySlot.SCHEDULED);
        order.setScheduledTime("19:00 - 20:00");
        order.setFullName("First User");
        order.setPhone("+216 20 000 000");
        order.setEmail("first@elfaddoui.test");
        order.setCity("Tunis");
        order.setArea("Centre");
        order.setStreet("Rue Habib Bourguiba");
        order.setPlaceType("Maison");
        order.setSubtotal(new BigDecimal("9.00"));
        order.setDeliveryFee(new BigDecimal("4.00"));
        order.setTotal(new BigDecimal("13.00"));
        order.setCourierName("Ali Ben Salem");
        order.setCourierPhone("+216 55 123 456");
        order.setStorePhone("+216 71 000 111");
        orderRepository.save(order);

        mockMvc.perform(get("/api/orders/{orderCode}/tracking", "ELF-20260424-0001")
                        .header("Authorization", "Bearer " + clientToken("second@elfaddoui.test")))
                .andExpect(status().isNotFound());
    }
}
