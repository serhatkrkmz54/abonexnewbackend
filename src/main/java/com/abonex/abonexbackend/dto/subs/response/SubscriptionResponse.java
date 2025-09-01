package com.abonex.abonexbackend.dto.subs.response;

import com.abonex.abonexbackend.entity.Subscription;
import com.abonex.abonexbackend.entity.enums.BillingCycle;
import com.abonex.abonexbackend.entity.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private String subscriptionName;
    private BigDecimal amount;
    private Currency currency;
    private BillingCycle billingCycle;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextPaymentDate;
    private String cardName;
    private String cardLastFourDigits;
    private int notificationDaysBefore;
    private boolean isActive;

    public static SubscriptionResponse fromEntity(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .subscriptionName(subscription.getSubscriptionName())
                .amount(subscription.getAmount())
                .currency(subscription.getCurrency())
                .billingCycle(subscription.getBillingCycle())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .nextPaymentDate(subscription.getNextPaymentDate())
                .cardName(subscription.getCardName())
                .cardLastFourDigits(subscription.getCardLastFourDigits())
                .notificationDaysBefore(subscription.getNotificationDaysBefore())
                .isActive(subscription.isActive())
                .build();
    }
}
