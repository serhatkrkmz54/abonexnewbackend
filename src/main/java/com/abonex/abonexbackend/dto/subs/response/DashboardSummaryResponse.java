package com.abonex.abonexbackend.dto.subs.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DashboardSummaryResponse {
    private BigDecimal totalMonthlyCost;
    private long activeSubscriptionCount;
    private SimpleSubscriptionDto mostExpensiveSubscription;
    private NextPaymentDto nextBigPayment;

    @Data
    @Builder
    public static class SimpleSubscriptionDto {
        private String name;
        private BigDecimal monthlyCost;
    }

    @Data
    @Builder
    public static class NextPaymentDto {
        private String name;
        private LocalDate nextPaymentDate;
        private BigDecimal amount;
    }
}
