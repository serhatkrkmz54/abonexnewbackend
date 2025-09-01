package com.abonex.abonexbackend.controller;

import com.abonex.abonexbackend.dto.admin.response.TemplateWithPlansResponse;
import com.abonex.abonexbackend.service.subs.SubscriptionTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription-templates")
@RequiredArgsConstructor
public class SubscriptionTemplateController {

    private final SubscriptionTemplateService templateService;

    @GetMapping
    public ResponseEntity<List<TemplateWithPlansResponse>> getAllTemplatesWithPlans() {
        List<TemplateWithPlansResponse> templates = templateService.getAllTemplatesWithPlans();
        return ResponseEntity.ok(templates);
    }

}
