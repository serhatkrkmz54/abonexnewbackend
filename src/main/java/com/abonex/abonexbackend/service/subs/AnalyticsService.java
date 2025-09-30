package com.abonex.abonexbackend.service.subs;

import com.abonex.abonexbackend.dto.subs.response.CardSpendingResponse;
import com.abonex.abonexbackend.dto.subs.response.DashboardSummaryResponse;
import com.abonex.abonexbackend.entity.PaymentHistory;
import com.abonex.abonexbackend.entity.Subscription;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.entity.enums.BillingCycle;
import com.abonex.abonexbackend.repository.PaymentHistoryRepository;
import com.abonex.abonexbackend.repository.SubscriptionRepository;
import com.abonex.abonexbackend.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AuthService authService;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        User user = authService.getAuthenticatedUser();
        List<Subscription> activeSubs = subscriptionRepository.findByUserAndIsActiveTrue(user);
        if (activeSubs.isEmpty()) {
            return DashboardSummaryResponse.builder()
                    .totalMonthlyCost(BigDecimal.ZERO)
                    .activeSubscriptionCount(0)
                    .build();
        }
        BigDecimal totalMonthlyCost = BigDecimal.ZERO;
        Subscription mostExpensiveSub = null;
        BigDecimal maxMonthlyCost = BigDecimal.ZERO;
        for (Subscription sub : activeSubs) {
            BigDecimal currentMonthlyCost = (sub.getBillingCycle() == BillingCycle.YEARLY)
                    ? sub.getAmount().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP)
                    : sub.getAmount();
            totalMonthlyCost = totalMonthlyCost.add(currentMonthlyCost);
            if (currentMonthlyCost.compareTo(maxMonthlyCost) > 0) {
                maxMonthlyCost = currentMonthlyCost;
                mostExpensiveSub = sub;
            }
        }
        Optional<Subscription> nextBigPaymentSubOpt = activeSubs.stream()
                .filter(sub -> sub.getNextPaymentDate() != null && !sub.getNextPaymentDate().isBefore(LocalDate.now()))
                .max(Comparator.comparing(Subscription::getAmount));
        DashboardSummaryResponse.SimpleSubscriptionDto mostExpensiveDto = null;
        if (mostExpensiveSub != null) {
            mostExpensiveDto = DashboardSummaryResponse.SimpleSubscriptionDto.builder()
                    .name(mostExpensiveSub.getSubscriptionName())
                    .monthlyCost(maxMonthlyCost)
                    .build();
        }
        DashboardSummaryResponse.NextPaymentDto nextBigPaymentDto = null;
        if (nextBigPaymentSubOpt.isPresent()) {
            Subscription nextBigPaymentSub = nextBigPaymentSubOpt.get();
            nextBigPaymentDto = DashboardSummaryResponse.NextPaymentDto.builder()
                    .name(nextBigPaymentSub.getSubscriptionName())
                    .nextPaymentDate(nextBigPaymentSub.getNextPaymentDate())
                    .amount(nextBigPaymentSub.getAmount())
                    .build();
        }

        return DashboardSummaryResponse.builder()
                .totalMonthlyCost(totalMonthlyCost)
                .activeSubscriptionCount(activeSubs.size())
                .mostExpensiveSubscription(mostExpensiveDto)
                .nextBigPayment(nextBigPaymentDto)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CardSpendingResponse> getSpendingByCard() {
        User user = authService.getAuthenticatedUser();

        List<PaymentHistory> allPayments = paymentHistoryRepository.findBySubscriptionUser(user);

        Map<String, BigDecimal> spendingByCard = allPayments.stream()
                .filter(p -> p.getSubscription().getCardName() != null && !p.getSubscription().getCardName().isBlank())
                .collect(Collectors.groupingBy(
                        p -> p.getSubscription().getCardName() + "|" + p.getSubscription().getCardLastFourDigits(),
                        Collectors.reducing(BigDecimal.ZERO, PaymentHistory::getAmountPaid, BigDecimal::add)
                ));

        return spendingByCard.entrySet().stream()
                .map(entry -> {
                    String[] cardInfo = entry.getKey().split("\\|");
                    return new CardSpendingResponse(cardInfo[0], cardInfo[1], entry.getValue());
                })
                .sorted(Comparator.comparing(CardSpendingResponse::getTotalAmount).reversed())
                .collect(Collectors.toList());
    }
}
