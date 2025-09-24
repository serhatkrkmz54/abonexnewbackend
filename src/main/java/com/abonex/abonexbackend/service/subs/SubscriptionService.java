package com.abonex.abonexbackend.service.subs;

import com.abonex.abonexbackend.dto.subs.request.CreateSubscriptionFromPlanRequest;
import com.abonex.abonexbackend.dto.subs.request.CreateSubscriptionRequest;
import com.abonex.abonexbackend.dto.subs.response.*;
import com.abonex.abonexbackend.entity.PaymentHistory;
import com.abonex.abonexbackend.entity.Subscription;
import com.abonex.abonexbackend.entity.SubscriptionPlan;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.entity.enums.BillingCycle;
import com.abonex.abonexbackend.entity.enums.NotificationType;
import com.abonex.abonexbackend.repository.PaymentHistoryRepository;
import com.abonex.abonexbackend.repository.SubscriptionPlanRepository;
import com.abonex.abonexbackend.repository.SubscriptionRepository;
import com.abonex.abonexbackend.service.auth.AuthService;
import com.abonex.abonexbackend.service.fcm.FCMService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final AuthService authService;
    private final SubscriptionPlanRepository planRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final FCMService fCMService;

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
    public HomeSubscriptionResponse getCategorizedHomeSubscriptions() {
        User user = authService.getAuthenticatedUser();
        List<Subscription> allActiveSubs = subscriptionRepository.findByUserAndIsActiveTrue(user);
        LocalDate today = LocalDate.now();
        LocalDate upcomingLimit = today.plusDays(7);

        List<SubscriptionResponse> overdue = new ArrayList<>();
        List<SubscriptionResponse> upcoming = new ArrayList<>();
        List<SubscriptionResponse> expired = new ArrayList<>();
        List<SubscriptionResponse> others = new ArrayList<>();

        for (Subscription sub : allActiveSubs) {
            if (sub.getNextPaymentDate() != null && sub.getNextPaymentDate().isBefore(today)) {
                overdue.add(SubscriptionResponse.fromEntity(sub));
            }
            else if (sub.getEndDate() != null && sub.getEndDate().isBefore(today)) {
                expired.add(SubscriptionResponse.fromEntity(sub));
            }
            else if (sub.getNextPaymentDate() != null && sub.getNextPaymentDate().isBefore(upcomingLimit)) {
                upcoming.add(SubscriptionResponse.fromEntity(sub));
            }
            else {
                others.add(SubscriptionResponse.fromEntity(sub));
            }
        }

        return HomeSubscriptionResponse.builder()
                .overdueSubscriptions(overdue)
                .upcomingPayments(upcoming)
                .expiredSubscriptions(expired)
                .otherSubscriptions(others)
                .build();
    }

    @Transactional(readOnly = true)
    public SubscriptionDetailsResponse getSubscriptionDetails(Long subscriptionId) {
        User user = authService.getAuthenticatedUser();

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Abonelik bulunamadı!"));

        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu işlem için yetkiniz yok!");
        }

        List<PaymentHistory> history = paymentHistoryRepository.findBySubscriptionOrderByPaymentDateDesc(subscription);

        SubscriptionResponse subDto = SubscriptionResponse.fromEntity(subscription);
        List<PaymentHistoryResponse> historyDto = history.stream()
                .map(PaymentHistoryResponse::fromEntity)
                .collect(Collectors.toList());

        return SubscriptionDetailsResponse.builder()
                .subscription(subDto)
                .paymentHistory(historyDto)
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

        if (request.isFirstPaymentMade()) {
            LocalDate nextPaymentDate = (request.getBillingCycle() == BillingCycle.MONTHLY)
                    ? request.getStartDate().plusMonths(1)
                    : request.getStartDate().plusYears(1);
            subscription.setNextPaymentDate(nextPaymentDate);

            Subscription savedSubscription = subscriptionRepository.save(subscription);
            PaymentHistory firstPayment = PaymentHistory.builder()
                    .subscription(savedSubscription)
                    .amountPaid(savedSubscription.getAmount())
                    .paymentDate(request.getStartDate().atStartOfDay())
                    .build();
            paymentHistoryRepository.save(firstPayment);
            return savedSubscription;

        } else {
            subscription.setNextPaymentDate(request.getStartDate());
            return subscriptionRepository.save(subscription);
        }
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

        User thisUser = subscription.getUser();
        if (thisUser.getFcmToken() != null && !thisUser.getFcmToken().isBlank()) {
            String title = "Ödeme Kaydedildi";
            String body = String.format(
                    "'%s' için %.2f %s tutarındaki ödemeniz başarıyla kaydedildi.",
                    subscription.getSubscriptionName(),
                    savedPayment.getAmountPaid(),
                    subscription.getCurrency()
            );
            Map<String, String> data = Map.of(
                    "notificationType", NotificationType.PAYMENT_CONFIRMED.name(),
                    "subscriptionId", subscription.getId().toString()
            );
            fCMService.sendNotificationWithData(user.getFcmToken(), title, body, data);
        }
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
                .logoUrl(plan.getTemplate().getLogoUrl())
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

        if (request.isFirstPaymentMade()) {
            LocalDate nextPaymentDate = (plan.getBillingCycle() == BillingCycle.MONTHLY)
                    ? request.getStartDate().plusMonths(1)
                    : request.getStartDate().plusYears(1);
            subscription.setNextPaymentDate(nextPaymentDate);
            Subscription savedSubscription = subscriptionRepository.save(subscription);

            PaymentHistory firstPayment = PaymentHistory.builder()
                    .subscription(savedSubscription)
                    .amountPaid(savedSubscription.getAmount())
                    .paymentDate(request.getStartDate().atStartOfDay())
                    .build();
            paymentHistoryRepository.save(firstPayment);

            return savedSubscription;

        } else {
            subscription.setNextPaymentDate(request.getStartDate());
            return subscriptionRepository.save(subscription);
        }
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
        User thisUser = subscription.getUser();
        if (thisUser.getFcmToken() != null && !thisUser.getFcmToken().isBlank()) {
            String title = "Abonelik İptal Edildi";
            String body = String.format("'%s' aboneliğiniz başarıyla iptal edildi.", subscription.getSubscriptionName());
            Map<String, String> data = Map.of(
                    "notificationType", NotificationType.SUBSCRIPTION_CANCELLED.name(),
                    "subscriptionId", subscription.getId().toString()
            );
            fCMService.sendNotificationWithData(user.getFcmToken(), title, body, data);
        }
    }

}
