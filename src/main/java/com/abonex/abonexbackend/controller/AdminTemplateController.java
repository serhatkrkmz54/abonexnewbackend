package com.abonex.abonexbackend.controller;

import com.abonex.abonexbackend.dto.admin.request.CreatePlanRequest;
import com.abonex.abonexbackend.dto.admin.request.CreateTemplateRequest;
import com.abonex.abonexbackend.entity.SubscriptionPlan;
import com.abonex.abonexbackend.entity.SubscriptionTemplate;
import com.abonex.abonexbackend.service.admin.AdminTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/templates")
@RequiredArgsConstructor
public class AdminTemplateController {
    private final AdminTemplateService adminTemplateService;

    @PostMapping("/create-template")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionTemplate> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        return new ResponseEntity<>(adminTemplateService.createTemplate(request), HttpStatus.CREATED);
    }

    @PostMapping("/{templateId}/plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlan> createPlanForTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody CreatePlanRequest request) {
        return new ResponseEntity<>(adminTemplateService.createPlanForTemplate(templateId, request), HttpStatus.CREATED);
    }
}
