package com.abonex.abonexbackend.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTemplateRequest {
    @NotBlank(message = "Şablon ismini belirtmelisiniz")
    private String name;
    private String category;
    private String logoUrl;
}
