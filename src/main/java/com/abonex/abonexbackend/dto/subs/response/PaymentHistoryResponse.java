package com.abonex.abonexbackend.dto.subs.response;

import com.abonex.abonexbackend.entity.PaymentHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentHistoryResponse {
    private Long id;
    private Long subscriptionId;
    private String subscriptionName;
    private LocalDateTime paymentDate;
    private BigDecimal amountPaid;

    public static PaymentHistoryResponse fromEntity(PaymentHistory paymentHistory) {
        if (paymentHistory == null) {
            return null;
        }

        return PaymentHistoryResponse.builder()
                .id(paymentHistory.getId())
                .subscriptionId(paymentHistory.getSubscription().getId())
                .subscriptionName(paymentHistory.getSubscription().getSubscriptionName())
                .paymentDate(paymentHistory.getPaymentDate())
                .amountPaid(paymentHistory.getAmountPaid())
                .build();
    }

}
