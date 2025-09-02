package com.abonex.abonexbackend.dto.subs.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;

@Data
@Builder
public class MonthlySpendResponse {
    private BigDecimal totalAmountSpent;
    private String currency;
    private Month month;
    private Year year;
}
