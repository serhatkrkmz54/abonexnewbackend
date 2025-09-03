package com.abonex.abonexbackend.dto.subs.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SubscriptionDetailsResponse {
    private SubscriptionResponse subscription;
    private List<PaymentHistoryResponse> paymentHistory;
}
