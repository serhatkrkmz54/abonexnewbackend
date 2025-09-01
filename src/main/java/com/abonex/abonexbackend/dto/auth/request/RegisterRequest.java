package com.abonex.abonexbackend.dto.auth.request;

import com.abonex.abonexbackend.entity.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "İsim girmek zorunludur")
    private String firstName;

    @NotBlank(message = "Soyisim girmek zorunludur")
    private String lastName;

    @NotBlank(message = "Email girmek zorunludur")
    @Email(message = "Email standartlarına uygun giriniz")
    private String email;

    @NotBlank(message = "Parola girmek zorunludur")
    private String password;

    @NotNull(message = "Doğum tarihi girmek zorunludur")
    private LocalDate dateOfBirth;

    @NotNull(message = "Cinsiyet seçimi zorunludur")
    private Gender gender;

    @NotNull(message = "Telefon numarası girmek zorunludur")
    private String phoneNumber;

    private String profileImageUrl;

}
