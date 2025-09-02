package com.abonex.abonexbackend.dto.admin.response;

import com.abonex.abonexbackend.entity.SubscriptionPlan;
import com.abonex.abonexbackend.entity.enums.BillingCycle;
import com.abonex.abonexbackend.entity.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PlanResponse {
    private Long id;
    private Long templateId;
    private String planName;
    private BigDecimal amount;
    private Currency currency;
    private BillingCycle billingCycle;

    public static PlanResponse fromEntity(SubscriptionPlan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .templateId(plan.getTemplate().getId())
                .planName(plan.getPlanName())
                .amount(plan.getAmount())
                .currency(plan.getCurrency())
                .billingCycle(plan.getBillingCycle())
                .build();
    }

}
