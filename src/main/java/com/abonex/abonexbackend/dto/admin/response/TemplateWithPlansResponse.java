package com.abonex.abonexbackend.dto.admin.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TemplateWithPlansResponse {
    private Long id;
    private String name;
    private String category;
    private String logoUrl;
    private List<PlanResponse> plans;
}
