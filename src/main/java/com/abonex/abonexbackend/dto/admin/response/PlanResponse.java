package com.abonex.abonexbackend.dto.admin.response;

import com.abonex.abonexbackend.entity.enums.BillingCycle;
import com.abonex.abonexbackend.entity.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PlanResponse {
    private Long id;
    private String planName;
    private BigDecimal amount;
    private Currency currency;
    private BillingCycle billingCycle;
}
