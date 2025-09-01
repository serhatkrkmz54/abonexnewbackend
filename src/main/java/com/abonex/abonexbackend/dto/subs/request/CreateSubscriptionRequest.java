package com.abonex.abonexbackend.dto.subs.request;

import com.abonex.abonexbackend.entity.enums.BillingCycle;
import com.abonex.abonexbackend.entity.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSubscriptionRequest {

    @NotBlank(message = "Abonelik adı boş olamaz.")
    @Size(min = 2, max = 100, message = "Abonelik adı 2 ile 100 karakter arasında olmalıdır.")
    private String subscriptionName;

    @NotNull(message = "Tutar boş olamaz.")
    @Positive(message = "Tutar pozitif bir değer olmalıdır.")
    private BigDecimal amount;

    @NotNull(message = "Para birimi boş olamaz.")
    private Currency currency;

    @NotNull(message = "Ödeme sıklığı boş olamaz.")
    private BillingCycle billingCycle;

    @NotNull(message = "Başlangıç tarihi boş olamaz.")
    private LocalDate startDate;

    private LocalDate endDate;

    private String cardName;

    @Pattern(regexp = "\\d{4}", message = "Kartın son 4 hanesi 4 rakamdan oluşmalıdır.")
    private String cardLastFourDigits;

    @Builder.Default
    private int notificationDaysBefore = 5;

}
