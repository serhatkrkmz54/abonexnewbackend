package com.abonex.abonexbackend.dto.auth.request;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateFcmTokenRequest {
    @NotBlank
    private String token;
}