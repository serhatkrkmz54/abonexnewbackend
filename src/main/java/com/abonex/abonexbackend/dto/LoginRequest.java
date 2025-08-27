package com.abonex.abonexbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email girişi zorunludur")
    @Email(message = "Email standartlarına uygun giriniz")
    private String email;

    @NotBlank(message = "Parola girişi zorunludur")
    private String password;
}
