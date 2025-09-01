package com.abonex.abonexbackend.dto.admin.request;

import com.abonex.abonexbackend.entity.enums.BillingCycle;
import com.abonex.abonexbackend.entity.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePlanRequest {
    @NotBlank
    private String planName;
    @NotNull @Positive
    private BigDecimal amount;
    @NotNull private Currency currency;
    @NotNull
    private BillingCycle billingCycle;
}
