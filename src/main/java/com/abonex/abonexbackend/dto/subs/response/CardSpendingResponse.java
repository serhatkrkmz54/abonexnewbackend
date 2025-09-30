package com.abonex.abonexbackend.dto.subs.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardSpendingResponse {
    private String cardName;
    private String cardLastFourDigits;
    private BigDecimal totalAmount;
}