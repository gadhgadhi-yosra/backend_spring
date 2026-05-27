package com.elfaddoui.backend.admin.service;

import com.elfaddoui.backend.admin.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminBackofficeService {
    AdminDashboardSummaryResponse getDashboardSummary();
    Page<AdminOrderSummaryResponse> getOrders(Pageable pageable);
    AdminOrderDetailResponse getOrder(Long id);
    AdminOrderDetailResponse updateOrderStatus(Long id, AdminOrderStatusUpdateRequest request);
    AdminOrderDetailResponse updateOrderCourier(Long id, AdminOrderCourierUpdateRequest request);
    Page<AdminCustomerSummaryResponse> getCustomers(Pageable pageable);
    AdminCustomerDetailResponse getCustomer(Long id);
    AdminDeliverySettingsResponse getDeliverySettings();
    AdminDeliverySettingsResponse updateDeliverySettings(AdminDeliverySettingsRequest request);
}
