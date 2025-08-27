package com.abonex.abonexbackend.dto.auth;

import com.abonex.abonexbackend.entity.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateRequest {
    private String firstName;

    private String lastName;

    @Email(message = "Email standartlarına uygun giriniz")
    private String email;

    private LocalDate dateOfBirth;

    private Gender gender;

    private String phoneNumber;

    private String profileImageUrl;
}
