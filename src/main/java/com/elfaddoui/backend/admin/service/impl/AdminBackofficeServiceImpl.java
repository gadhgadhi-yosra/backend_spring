package com.elfaddoui.backend.admin.service.impl;

import com.elfaddoui.backend.admin.dto.*;
import com.elfaddoui.backend.admin.service.AdminBackofficeService;
import com.elfaddoui.backend.appconfig.service.AppConfigService;
import com.elfaddoui.backend.category.repository.CategoryRepository;
import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.notification.service.NotificationService;
import com.elfaddoui.backend.order.entity.Order;
import com.elfaddoui.backend.order.entity.OrderItem;
import com.elfaddoui.backend.order.entity.OrderStatus;
import com.elfaddoui.backend.order.repository.OrderRepository;
import com.elfaddoui.backend.order.service.OrderEventService;
import com.elfaddoui.backend.order.service.OrderService;
import com.elfaddoui.backend.product.repository.ProductRepository;
import com.elfaddoui.backend.user.entity.Role;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AdminBackofficeServiceImpl implements AdminBackofficeService {

    public static final String DELIVERY_COURIER_NAME_KEY = "delivery.courier.name";
    public static final String DELIVERY_COURIER_PHONE_KEY = "delivery.courier.phone";
    public static final String DELIVERY_STORE_PHONE_KEY = "delivery.store.phone";
    public static final String DELIVERY_FEE_KEY = "delivery.fee";
    public static final String DELIVERY_ETA_LABEL_KEY = "delivery.eta.label";
    public static final String DEFAULT_COURIER_NAME = "Ali Ben Salem";
    public static final String DEFAULT_COURIER_PHONE = "+216 55 123 456";
    public static final String DEFAULT_STORE_PHONE = "+216 71 000 111";
    public static final BigDecimal DEFAULT_DELIVERY_FEE = new BigDecimal("4.00");
    public static final String DEFAULT_ETA_LABEL = "45-60 min";

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AppConfigService appConfigService;
    private final OrderService orderService;
    private final OrderEventService orderEventService;
    private final NotificationService notificationService;

    public AdminBackofficeServiceImpl(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            AppConfigService appConfigService,
            OrderService orderService,
            OrderEventService orderEventService,
            NotificationService notificationService
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.appConfigService = appConfigService;
        this.orderService = orderService;
        this.orderEventService = orderEventService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getDashboardSummary() {
        long categoriesCount = categoryRepository.count();
        long activeCategoriesCount = categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc().size();
        List<com.elfaddoui.backend.product.entity.Product> products = productRepository.findAll();
        long productsCount = products.size();
        long activeProductsCount = products.stream().filter(com.elfaddoui.backend.product.entity.Product::isActive).count();
        long lowStockProductsCount = products.stream()
                .filter(product -> product.getStockQty() != null && product.getStockQty() > 0 && product.getStockQty() <= 5)
                .count();
        long outOfStockProductsCount = products.stream()
                .filter(product -> product.getStockQty() == null || product.getStockQty() <= 0)
                .count();
        long clientsCount = userRepository.findByRole(Role.CLIENT, Pageable.unpaged()).getTotalElements();

        List<Order> orders = orderRepository.findAll();
        BigDecimal totalRevenue = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Instant todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        BigDecimal todayRevenue = orders.stream()
                .filter(order -> order.getCreatedAt() != null && !order.getCreatedAt().isBefore(todayStart))
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AdminDashboardSummaryResponse(
                categoriesCount,
                activeCategoriesCount,
                productsCount,
                activeProductsCount,
                lowStockProductsCount,
                outOfStockProductsCount,
                clientsCount,
                orders.size(),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByStatus(OrderStatus.CONFIRMED),
                orderRepository.countByStatus(OrderStatus.PREPARING),
                orderRepository.countByStatus(OrderStatus.SHIPPED),
                orderRepository.countByStatus(OrderStatus.DELIVERED),
                orderRepository.countByStatus(OrderStatus.CANCELLED),
                totalRevenue,
                todayRevenue
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminOrderSummaryResponse> getOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toOrderSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderDetailResponse getOrder(Long id) {
        return toOrderDetail(findOrder(id));
    }

    @Override
    public AdminOrderDetailResponse updateOrderStatus(Long id, AdminOrderStatusUpdateRequest request) {
        Order order = findOrder(id);
        OrderStatus nextStatus = OrderStatus.valueOf(request.status().trim().toUpperCase());
        if (order.getStatus() != nextStatus) {
            order.setStatus(nextStatus);
            notificationService.notifyOrderStatusChanged(order);
        }
        orderEventService.publish(
                order.getReference(),
                orderService.buildTrackingResponse(order)
        );
        return toOrderDetail(order);
    }

    @Override
    public AdminOrderDetailResponse updateOrderCourier(Long id, AdminOrderCourierUpdateRequest request) {
        Order order = findOrder(id);
        order.setCourierName(request.courierName().trim());
        order.setCourierPhone(request.courierPhone().trim());
        orderEventService.publish(
                order.getReference(),
                orderService.buildTrackingResponse(order)
        );
        return toOrderDetail(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCustomerSummaryResponse> getCustomers(Pageable pageable) {
        return userRepository.findByRole(Role.CLIENT, pageable)
                .map(this::toCustomerSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCustomerDetailResponse getCustomer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        return toCustomerDetail(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDeliverySettingsResponse getDeliverySettings() {
        return new AdminDeliverySettingsResponse(
                appConfigService.getValue(DELIVERY_COURIER_NAME_KEY, DEFAULT_COURIER_NAME),
                appConfigService.getValue(DELIVERY_COURIER_PHONE_KEY, DEFAULT_COURIER_PHONE),
                appConfigService.getValue(DELIVERY_STORE_PHONE_KEY, DEFAULT_STORE_PHONE),
                parseBigDecimal(appConfigService.getValue(DELIVERY_FEE_KEY, DEFAULT_DELIVERY_FEE.toPlainString()), DEFAULT_DELIVERY_FEE),
                appConfigService.getValue(DELIVERY_ETA_LABEL_KEY, DEFAULT_ETA_LABEL)
        );
    }

    @Override
    public AdminDeliverySettingsResponse updateDeliverySettings(AdminDeliverySettingsRequest request) {
        appConfigService.setValue(DELIVERY_COURIER_NAME_KEY, request.courierName().trim());
        appConfigService.setValue(DELIVERY_COURIER_PHONE_KEY, request.courierPhone().trim());
        appConfigService.setValue(DELIVERY_STORE_PHONE_KEY, request.storePhone().trim());
        appConfigService.setValue(DELIVERY_FEE_KEY, request.deliveryFee().toPlainString());
        appConfigService.setValue(DELIVERY_ETA_LABEL_KEY, request.etaLabel().trim());
        return getDeliverySettings();
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    private AdminOrderSummaryResponse toOrderSummary(Order order) {
        return new AdminOrderSummaryResponse(
                order.getId(),
                order.getReference(),
                order.getStatus().name(),
                order.getTotal(),
                order.getFullName(),
                order.getPhone(),
                order.getPaymentMethod().name(),
                order.getDeliverySlot().name(),
                order.getCourierName(),
                order.getCreatedAt()
        );
    }

    private AdminOrderDetailResponse toOrderDetail(Order order) {
        List<AdminOrderItemResponse> items = order.getItems().stream()
                .map(this::toOrderItem)
                .toList();

        return new AdminOrderDetailResponse(
                order.getId(),
                order.getReference(),
                order.getStatus().name(),
                order.getPaymentMethod().name(),
                order.getDeliverySlot().name(),
                order.getScheduledTime(),
                order.getFullName(),
                order.getPhone(),
                order.getEmail(),
                order.getNote(),
                order.getCity(),
                order.getArea(),
                order.getStreet(),
                order.getExtra(),
                order.getPostalCode(),
                order.getAddressHint(),
                order.getPlaceType(),
                order.getSubtotal(),
                order.getDeliveryFee(),
                order.getTotal(),
                order.getCourierName(),
                order.getCourierPhone(),
                order.getStorePhone(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }

    private AdminOrderItemResponse toOrderItem(OrderItem item) {
        return new AdminOrderItemResponse(
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getProductNameSnapshot(),
                item.getQty(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    private AdminCustomerSummaryResponse toCustomerSummary(User user) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        BigDecimal totalSpent = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant lastOrderAt = orders.stream()
                .map(Order::getCreatedAt)
                .filter(createdAt -> createdAt != null)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new AdminCustomerSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.isEnabled(),
                orders.size(),
                totalSpent,
                lastOrderAt
        );
    }

    private AdminCustomerDetailResponse toCustomerDetail(User user) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        BigDecimal totalSpent = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant lastOrderAt = orders.stream()
                .map(Order::getCreatedAt)
                .filter(createdAt -> createdAt != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
        Set<String> roles = user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());

        return new AdminCustomerDetailResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getAddress(),
                user.isEnabled(),
                roles,
                orders.size(),
                totalSpent,
                lastOrderAt
        );
    }

    private BigDecimal parseBigDecimal(String rawValue, BigDecimal fallback) {
        try {
            return new BigDecimal(rawValue);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
