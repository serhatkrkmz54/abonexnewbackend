package com.abonex.abonexbackend.dto.subs.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateSubscriptionFromPlanRequest {
    @NotNull private Long planId;
    @NotNull
    private LocalDate startDate;
    private String cardName;
    private String cardLastFourDigits;
    private int notificationDaysBefore = 5;
}
