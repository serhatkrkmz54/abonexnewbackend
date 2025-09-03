package com.abonex.abonexbackend.controller;

import com.abonex.abonexbackend.dto.subs.request.CreateSubscriptionFromPlanRequest;
import com.abonex.abonexbackend.dto.subs.request.CreateSubscriptionRequest;
import com.abonex.abonexbackend.dto.subs.response.*;
import com.abonex.abonexbackend.entity.PaymentHistory;
import com.abonex.abonexbackend.entity.Subscription;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.service.subs.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/get-subs")
    public ResponseEntity<List<SubscriptionResponse>> getUserSubscriptions(@AuthenticationPrincipal User user) {
        List<Subscription> subscriptions = subscriptionService.getUserSubscriptions(user);
        List<SubscriptionResponse> response = subscriptions.stream()
                .map(SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly-cost")
    public ResponseEntity<MonthlySpendResponse> getTotalMonthlyCost() {
        MonthlySpendResponse response = subscriptionService.calculateMonthlyCostOfActiveSubscriptions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categorized-home-view")
    public ResponseEntity<HomeSubscriptionResponse> getCategorizedHomeView() {
        HomeSubscriptionResponse response = subscriptionService.getCategorizedHomeSubscriptions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subscription-detail/{id}")
    public ResponseEntity<SubscriptionDetailsResponse> getSubscriptionDetails(@PathVariable Long id) {
        SubscriptionDetailsResponse response = subscriptionService.getSubscriptionDetails(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/manual")
    public ResponseEntity<SubscriptionResponse> createManualSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        Subscription newSubscription = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SubscriptionResponse.fromEntity(newSubscription));
    }

    @PostMapping("/from-plan")
    public ResponseEntity<SubscriptionResponse> createSubscriptionFromPlan(@Valid @RequestBody CreateSubscriptionFromPlanRequest request) {
        Subscription newSubscription = subscriptionService.createSubscriptionFromPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SubscriptionResponse.fromEntity(newSubscription));
    }

    @PostMapping("/{subscriptionId}/log-payment")
    public ResponseEntity<PaymentHistoryResponse> logPaymentForSubscription(@PathVariable Long subscriptionId) {
        PaymentHistory paymentHistory = subscriptionService.logPayment(subscriptionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentHistoryResponse.fromEntity(paymentHistory));
    }

    @DeleteMapping("/deactive/{subscriptionId}")
    public ResponseEntity<Void> cancelSubscription(@PathVariable Long subscriptionId) {
        subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }

}
