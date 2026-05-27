package com.elfaddoui.backend.admin.controller;

import com.elfaddoui.backend.admin.dto.*;
import com.elfaddoui.backend.admin.service.AdminBackofficeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminOrderController {

    private final AdminBackofficeService adminBackofficeService;

    public AdminOrderController(AdminBackofficeService adminBackofficeService) {
        this.adminBackofficeService = adminBackofficeService;
    }

    @GetMapping
    public Page<AdminOrderSummaryResponse> getPage(Pageable pageable) {
        return adminBackofficeService.getOrders(pageable);
    }

    @GetMapping("/{id}")
    public AdminOrderDetailResponse getById(@PathVariable Long id) {
        return adminBackofficeService.getOrder(id);
    }

    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AdminOrderDetailResponse updateStatus(@PathVariable Long id, @Valid @RequestBody AdminOrderStatusUpdateRequest request) {
        return adminBackofficeService.updateOrderStatus(id, request);
    }

    @PatchMapping(value = "/{id}/courier", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AdminOrderDetailResponse updateCourier(@PathVariable Long id, @Valid @RequestBody AdminOrderCourierUpdateRequest request) {
        return adminBackofficeService.updateOrderCourier(id, request);
    }
}
