package com.abonex.abonexbackend.repository;

import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.entity.VerificationCode;
import com.abonex.abonexbackend.entity.enums.VerificationCodeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findFirstByUserAndTypeOrderByExpiryDateDesc(User user, VerificationCodeType type);
}
