package com.abonex.abonexbackend.dto.subs.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HomeSubscriptionResponse {

    private List<SubscriptionResponse> overdueSubscriptions;
    private List<SubscriptionResponse> upcomingPayments;
    private List<SubscriptionResponse> expiredSubscriptions;
    private List<SubscriptionResponse> otherSubscriptions;

}
