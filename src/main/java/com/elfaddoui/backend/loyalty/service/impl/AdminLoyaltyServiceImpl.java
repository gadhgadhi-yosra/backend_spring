package com.elfaddoui.backend.loyalty.service.impl;

import com.elfaddoui.backend.admin.dto.AdminLoyaltyAccountResponse;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyCustomerSummaryResponse;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyGiftRequest;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyGiftResponse;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyPointAdjustmentRequest;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyVoucherRequest;
import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.loyalty.dto.LoyaltyVoucherResponse;
import com.elfaddoui.backend.loyalty.entity.LoyaltyAccount;
import com.elfaddoui.backend.loyalty.entity.LoyaltyGift;
import com.elfaddoui.backend.loyalty.entity.LoyaltyLedgerEntry;
import com.elfaddoui.backend.loyalty.entity.LoyaltyVoucher;
import com.elfaddoui.backend.loyalty.repository.LoyaltyAccountRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyGiftRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyLedgerEntryRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyVoucherRepository;
import com.elfaddoui.backend.loyalty.service.AdminLoyaltyService;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.entity.Role;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminLoyaltyServiceImpl implements AdminLoyaltyService {

    private final UserRepository userRepository;
    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyLedgerEntryRepository ledgerEntryRepository;
    private final LoyaltyVoucherRepository voucherRepository;
    private final LoyaltyGiftRepository giftRepository;

    public AdminLoyaltyServiceImpl(
            UserRepository userRepository,
            LoyaltyAccountRepository accountRepository,
            LoyaltyLedgerEntryRepository ledgerEntryRepository,
            LoyaltyVoucherRepository voucherRepository,
            LoyaltyGiftRepository giftRepository
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.voucherRepository = voucherRepository;
        this.giftRepository = giftRepository;
    }

    @Override
    public AdminLoyaltyAccountResponse getCustomerAccount(Long customerId) {
        return toAccountResponse(accountForCustomer(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminLoyaltyCustomerSummaryResponse> searchCustomers(String query) {
        String q = (query == null) ? null : query.trim();
        List<User> users;
        if (q == null || q.isBlank()) {
            users = userRepository.findByRole(Role.CLIENT, PageRequest.of(0, 20)).getContent();
        } else {
            users = userRepository.searchByRoleAndQuery(Role.CLIENT, q, PageRequest.of(0, 20)).getContent();
        }
        if (users.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = users.stream().map(User::getId).toList();
        Map<Long, LoyaltyAccount> accountByUserId = new HashMap<>();
        for (LoyaltyAccount account : accountRepository.findByUserIdIn(userIds)) {
            accountByUserId.put(account.getUser().getId(), account);
        }

        return users.stream().map(u -> {
            LoyaltyAccount account = accountByUserId.get(u.getId());
            return new AdminLoyaltyCustomerSummaryResponse(
                    u.getId(),
                    u.getFullName(),
                    u.getEmail(),
                    account == null ? null : account.getCardNumber(),
                    account == null ? 0 : account.getPointsBalance()
            );
        }).toList();
    }

    @Override
    public AdminLoyaltyAccountResponse adjustPoints(Long customerId, AdminLoyaltyPointAdjustmentRequest request) {
        if (request.points() == 0) {
            throw new IllegalStateException("Points adjustment cannot be zero");
        }

        LoyaltyAccount account = accountForCustomer(customerId);
        int nextBalance = account.getPointsBalance() + request.points();
        if (nextBalance < 0) {
            throw new IllegalStateException("Points balance cannot be negative");
        }

        account.setPointsBalance(nextBalance);
        ledgerEntryRepository.save(new LoyaltyLedgerEntry(account, request.title().trim(), request.points()));
        return toAccountResponse(account);
    }

    @Override
    public LoyaltyVoucherResponse createVoucher(Long customerId, AdminLoyaltyVoucherRequest request) {
        LoyaltyAccount account = accountForCustomer(customerId);
        LoyaltyVoucher voucher = voucherRepository.save(new LoyaltyVoucher(
                account,
                request.title().trim(),
                request.code().trim().toUpperCase(),
                request.description(),
                request.expiresAt()
        ));
        return toVoucherResponse(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminLoyaltyGiftResponse> getGifts() {
        return giftRepository.findAllByOrderBySortOrderAscPointsAsc()
                .stream()
                .map(this::toAdminGiftResponse)
                .toList();
    }

    @Override
    public AdminLoyaltyGiftResponse createGift(AdminLoyaltyGiftRequest request) {
        LoyaltyGift gift = new LoyaltyGift(request.title().trim(), request.points());
        gift.setActive(request.active() == null || request.active());
        gift.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        return toAdminGiftResponse(giftRepository.save(gift));
    }

    @Override
    public AdminLoyaltyGiftResponse updateGift(Long giftId, AdminLoyaltyGiftRequest request) {
        LoyaltyGift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new NotFoundException("Gift not found"));
        gift.setTitle(request.title().trim());
        gift.setPoints(request.points());
        gift.setActive(request.active() == null || request.active());
        gift.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        return toAdminGiftResponse(gift);
    }

    private LoyaltyAccount accountForCustomer(Long customerId) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        return accountRepository.findByUserId(user.getId())
                .orElseGet(() -> accountRepository.save(new LoyaltyAccount(user, cardNumberFor(user.getId()))));
    }

    private String cardNumberFor(Long userId) {
        String candidate = "201" + String.format("%013d", userId);
        if (!accountRepository.existsByCardNumber(candidate)) {
            return candidate;
        }
        return "201" + String.format("%013d", Math.abs(System.nanoTime()) % 10_000_000_000_000L);
    }

    private AdminLoyaltyAccountResponse toAccountResponse(LoyaltyAccount account) {
        User user = account.getUser();
        return new AdminLoyaltyAccountResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                account.getCardNumber(),
                account.getPointsBalance()
        );
    }

    private LoyaltyVoucherResponse toVoucherResponse(LoyaltyVoucher voucher) {
        return new LoyaltyVoucherResponse(
                voucher.getTitle(),
                voucher.getCode(),
                voucher.getDescription(),
                voucher.getExpiresAt()
        );
    }

    private AdminLoyaltyGiftResponse toAdminGiftResponse(LoyaltyGift gift) {
        return new AdminLoyaltyGiftResponse(
                gift.getId(),
                gift.getTitle(),
                gift.getPoints(),
                gift.isActive(),
                gift.getSortOrder()
        );
    }
}
