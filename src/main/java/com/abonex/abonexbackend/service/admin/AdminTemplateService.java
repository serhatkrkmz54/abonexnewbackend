package com.abonex.abonexbackend.service.admin;

import com.abonex.abonexbackend.dto.admin.request.CreatePlanRequest;
import com.abonex.abonexbackend.dto.admin.request.CreateTemplateRequest;
import com.abonex.abonexbackend.entity.SubscriptionPlan;
import com.abonex.abonexbackend.entity.SubscriptionTemplate;
import com.abonex.abonexbackend.repository.SubscriptionPlanRepository;
import com.abonex.abonexbackend.repository.SubscriptionTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminTemplateService {

    private final SubscriptionTemplateRepository templateRepository;
    private final SubscriptionPlanRepository planRepository;

    public SubscriptionTemplate createTemplate(CreateTemplateRequest request) {
        SubscriptionTemplate template = SubscriptionTemplate.builder()
                .name(request.getName())
                .category(request.getCategory())
                .logoUrl(request.getLogoUrl())
                .build();
        return templateRepository.save(template);
    }
    public SubscriptionPlan createPlanForTemplate(Long templateId, CreatePlanRequest request) {
        SubscriptionTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bu id ile şablon bulunamadı: " + templateId));

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .template(template)
                .planName(request.getPlanName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .billingCycle(request.getBillingCycle())
                .build();

        template.getPlans().add(plan);
        return planRepository.save(plan);
    }
}
