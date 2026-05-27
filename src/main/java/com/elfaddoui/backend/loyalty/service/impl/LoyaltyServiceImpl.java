package com.elfaddoui.backend.loyalty.service.impl;

import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.loyalty.dto.LoyaltyGiftResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyHistoryResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyMeResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyVoucherResponse;
import com.elfaddoui.backend.loyalty.entity.LoyaltyAccount;
import com.elfaddoui.backend.loyalty.entity.LoyaltyGift;
import com.elfaddoui.backend.loyalty.entity.LoyaltyLedgerEntry;
import com.elfaddoui.backend.loyalty.repository.LoyaltyAccountRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyGiftRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyLedgerEntryRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyVoucherRepository;
import com.elfaddoui.backend.loyalty.service.LoyaltyService;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional
public class LoyaltyServiceImpl implements LoyaltyService {

    private final UserRepository userRepository;
    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyLedgerEntryRepository ledgerEntryRepository;
    private final LoyaltyVoucherRepository voucherRepository;
    private final LoyaltyGiftRepository giftRepository;

    public LoyaltyServiceImpl(
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
    public LoyaltyMeResponse me(String userEmail) {
        LoyaltyAccount account = accountFor(userEmail);
        MonthWindow month = currentMonthWindow();
        List<LoyaltyLedgerEntry> monthEntries = ledgerEntryRepository.findByAccountAndCreatedAtBetween(
                account,
                month.start(),
                month.end()
        );
        int earnedThisMonth = monthEntries.stream()
                .mapToInt(LoyaltyLedgerEntry::getPoints)
                .filter(points -> points > 0)
                .sum();
        int usedThisMonth = Math.abs(monthEntries.stream()
                .mapToInt(LoyaltyLedgerEntry::getPoints)
                .filter(points -> points < 0)
                .sum());
        int nextGiftPoints = giftRepository
                .findFirstByActiveTrueAndPointsGreaterThanOrderByPointsAsc(account.getPointsBalance())
                .map(LoyaltyGift::getPoints)
                .map(points -> points - account.getPointsBalance())
                .orElse(0);

        return new LoyaltyMeResponse(
                account.getPointsBalance(),
                account.getCardNumber(),
                earnedThisMonth,
                usedThisMonth,
                nextGiftPoints
        );
    }

    @Override
    public List<LoyaltyHistoryResponse> history(String userEmail) {
        LoyaltyAccount account = accountFor(userEmail);
        return ledgerEntryRepository.findByAccountOrderByCreatedAtDesc(account)
                .stream()
                .map(entry -> new LoyaltyHistoryResponse(entry.getTitle(), entry.getCreatedAt(), entry.getPoints()))
                .toList();
    }

    @Override
    public List<LoyaltyVoucherResponse> vouchers(String userEmail) {
        LoyaltyAccount account = accountFor(userEmail);
        return voucherRepository.findByAccountOrderByExpiresAtAsc(account)
                .stream()
                .map(voucher -> new LoyaltyVoucherResponse(
                        voucher.getTitle(),
                        voucher.getCode(),
                        voucher.getDescription(),
                        voucher.getExpiresAt()
                ))
                .toList();
    }

    @Override
    public List<LoyaltyGiftResponse> gifts(String userEmail) {
        LoyaltyAccount account = accountFor(userEmail);
        return giftRepository.findByActiveTrueOrderBySortOrderAscPointsAsc()
                .stream()
                .map(gift -> new LoyaltyGiftResponse(
                        gift.getTitle(),
                        gift.getPoints(),
                        account.getPointsBalance() >= gift.getPoints()
                ))
                .toList();
    }

    private LoyaltyAccount accountFor(String userEmail) {
        return accountRepository.findByUserEmailIgnoreCase(userEmail)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new NotFoundException("User not found"));
                    return accountRepository.save(new LoyaltyAccount(user, cardNumberFor(user.getId())));
                });
    }

    private String cardNumberFor(Long userId) {
        String candidate = "201" + String.format("%013d", userId);
        if (!accountRepository.existsByCardNumber(candidate)) {
            return candidate;
        }
        return "201" + String.format("%013d", Math.abs(System.nanoTime()) % 10_000_000_000_000L);
    }

    private MonthWindow currentMonthWindow() {
        YearMonth month = YearMonth.now(ZoneOffset.UTC);
        Instant start = month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = month.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return new MonthWindow(start, end);
    }

    private record MonthWindow(Instant start, Instant end) {
    }
}
