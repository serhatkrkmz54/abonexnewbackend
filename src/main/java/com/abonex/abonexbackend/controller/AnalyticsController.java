package com.abonex.abonexbackend.controller;

import com.abonex.abonexbackend.dto.subs.response.CardSpendingResponse;
import com.abonex.abonexbackend.dto.subs.response.DashboardSummaryResponse;
import com.abonex.abonexbackend.service.subs.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard-summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/spending-by-card")
    public ResponseEntity<List<CardSpendingResponse>> getSpendingByCard() {
        return ResponseEntity.ok(analyticsService.getSpendingByCard());
    }
}
