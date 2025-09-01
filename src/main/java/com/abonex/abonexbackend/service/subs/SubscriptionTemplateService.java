package com.abonex.abonexbackend.service.subs;

import com.abonex.abonexbackend.dto.admin.response.PlanResponse;
import com.abonex.abonexbackend.dto.admin.response.TemplateWithPlansResponse;
import com.abonex.abonexbackend.entity.SubscriptionTemplate;
import com.abonex.abonexbackend.repository.SubscriptionTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionTemplateService {
    private final SubscriptionTemplateRepository templateRepository;

    @Transactional(readOnly = true) // Bu metod sadece veri okuduğu için performansı artırır.
    public List<TemplateWithPlansResponse> getAllTemplatesWithPlans() {
        // 1. Veritabanından tüm SubscriptionTemplate'leri çek.
        //    (Entity'de FetchType.EAGER kullandığımız için planları da birlikte gelecektir.)
        List<SubscriptionTemplate> templates = templateRepository.findAll();

        // 2. Entity listesini DTO listesine dönüştür.
        return templates.stream()
                .map(this::convertToTemplateWithPlansResponse)
                .collect(Collectors.toList());
    }

    private TemplateWithPlansResponse convertToTemplateWithPlansResponse(SubscriptionTemplate template) {
        // Template'e ait planları (SubscriptionPlan entity'leri) PlanResponse DTO'larına dönüştür.
        List<PlanResponse> planResponses = template.getPlans().stream()
                .map(plan -> PlanResponse.builder()
                        .id(plan.getId())
                        .planName(plan.getPlanName())
                        .amount(plan.getAmount())
                        .currency(plan.getCurrency())
                        .billingCycle(plan.getBillingCycle())
                        .build())
                .collect(Collectors.toList());

        // Ana Template bilgilerini ve dönüştürülmüş plan listesini kullanarak DTO'yu oluştur ve döndür.
        return TemplateWithPlansResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .category(template.getCategory())
                .logoUrl(template.getLogoUrl())
                .plans(planResponses)
                .build();
    }
}
