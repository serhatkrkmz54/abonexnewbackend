package com.abonex.abonexbackend.service.subs;

import com.abonex.abonexbackend.dto.subs.request.CreateSubscriptionFromPlanRequest;
import com.abonex.abonexbackend.dto.subs.request.CreateSubscriptionRequest;
import com.abonex.abonexbackend.dto.subs.response.MonthlySpendResponse;
import com.abonex.abonexbackend.entity.PaymentHistory;
import com.abonex.abonexbackend.entity.Subscription;
import com.abonex.abonexbackend.entity.SubscriptionPlan;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.entity.enums.BillingCycle;
import com.abonex.abonexbackend.repository.PaymentHistoryRepository;
import com.abonex.abonexbackend.repository.SubscriptionPlanRepository;
import com.abonex.abonexbackend.repository.SubscriptionRepository;
import com.abonex.abonexbackend.service.auth.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final AuthService authService;
    private final SubscriptionPlanRepository planRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    public List<Subscription> getUserSubscriptions(User user){
        User authenticatedUser = authService.getAuthenticatedUser();
        return subscriptionRepository.findByUserAndIsActiveTrue(authenticatedUser);
    }

    @Transactional(readOnly = true)
    public MonthlySpendResponse calculateMonthlyCostOfActiveSubscriptions() {
        User user = authService.getAuthenticatedUser();

        List<Subscription> activeSubscriptions = subscriptionRepository.findByUserAndIsActiveTrue(user);

        BigDecimal totalMonthlyCost = BigDecimal.ZERO;

        for (Subscription sub : activeSubscriptions) {
            if (sub.getBillingCycle() == BillingCycle.MONTHLY) {
                totalMonthlyCost = totalMonthlyCost.add(sub.getAmount());
            } else if (sub.getBillingCycle() == BillingCycle.YEARLY) {
                BigDecimal monthlyEquivalent = sub.getAmount().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
                totalMonthlyCost = totalMonthlyCost.add(monthlyEquivalent);
            }
        }
        return MonthlySpendResponse.builder()
                .totalAmountSpent(totalMonthlyCost)
                .currency("TRY")
                .month(LocalDate.now().getMonth())
                .year(Year.of(LocalDate.now().getYear()))
                .build();
    }

    @Transactional
    public Subscription createSubscription(CreateSubscriptionRequest request) {
        User user = authService.getAuthenticatedUser();
        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionName(request.getSubscriptionName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .billingCycle(request.getBillingCycle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .cardName(request.getCardName())
                .cardLastFourDigits(request.getCardLastFourDigits())
                .notificationDaysBefore(request.getNotificationDaysBefore())
                .isActive(true)
                .nextPaymentDate(request.getStartDate())
                .build();
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public PaymentHistory logPayment(Long subscriptionId){
        User user = authService.getAuthenticatedUser();
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Abonelik bulunamadı!"));

        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Bu işlem için yetkiniz yok!");
        }

        PaymentHistory paymentToSave = PaymentHistory.builder()
                .subscription(subscription)
                .amountPaid(subscription.getAmount())
                .paymentDate(LocalDateTime.now())
                .build();
        PaymentHistory savedPayment = paymentHistoryRepository.save(paymentToSave);

        LocalDate newNextPaymentDate = (subscription.getBillingCycle() == BillingCycle.MONTHLY)
                ? subscription.getNextPaymentDate().plusMonths(1)
                : subscription.getNextPaymentDate().plusYears(1);

        subscription.setNextPaymentDate(newNextPaymentDate);
        subscriptionRepository.save(subscription);
        return savedPayment;
    }

    @Transactional
    public Subscription createSubscriptionFromPlan(CreateSubscriptionFromPlanRequest request) {
        User user = authService.getAuthenticatedUser();

        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new EntityNotFoundException("Bu id ile plan bulunamadı: " + request.getPlanId()));

        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionName(plan.getTemplate().getName() + " - " + plan.getPlanName())
                .amount(plan.getAmount())
                .currency(plan.getCurrency())
                .billingCycle(plan.getBillingCycle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextPaymentDate(request.getStartDate())
                .cardName(request.getCardName())
                .cardLastFourDigits(request.getCardLastFourDigits())
                .notificationDaysBefore(request.getNotificationDaysBefore())
                .isActive(true)
                .build();

        return subscriptionRepository.save(subscription);
    }

    public void cancelSubscription(Long subscriptionId) {
        User user = authService.getAuthenticatedUser();
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Abonelik bulunamadı!"));
        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu işlem için yetkiniz yok!");
        }

        subscription.setActive(false);
        subscription.setEndDate(LocalDate.now());
        subscription.setNextPaymentDate(null);
        subscriptionRepository.save(subscription);
    }

}
