package com.elfaddoui.backend.admin.controller;

import com.elfaddoui.backend.admin.dto.AdminCustomerDetailResponse;
import com.elfaddoui.backend.admin.dto.AdminCustomerSummaryResponse;
import com.elfaddoui.backend.admin.service.AdminBackofficeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin/customers", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminCustomerController {

    private final AdminBackofficeService adminBackofficeService;

    public AdminCustomerController(AdminBackofficeService adminBackofficeService) {
        this.adminBackofficeService = adminBackofficeService;
    }

    @GetMapping
    public Page<AdminCustomerSummaryResponse> getPage(Pageable pageable) {
        return adminBackofficeService.getCustomers(pageable);
    }

    @GetMapping("/{id}")
    public AdminCustomerDetailResponse getById(@PathVariable Long id) {
        return adminBackofficeService.getCustomer(id);
    }
}
