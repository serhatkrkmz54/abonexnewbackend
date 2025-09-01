package com.abonex.abonexbackend.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReactivateRequest {
    @NotBlank(message = "Email girmek zorunludur")
    @Email(message = "Email standartlarına uygun giriniz")
    private String email;

    @NotBlank(message = "Parola girmek zorunludur")
    private String password;
}
