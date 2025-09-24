package com.abonex.abonexbackend.service.auth;

import com.abonex.abonexbackend.dto.auth.request.VerifyCodeRequest;
import com.abonex.abonexbackend.dto.auth.response.AuthResponse;
import com.abonex.abonexbackend.dto.auth.request.LoginRequest;
import com.abonex.abonexbackend.dto.auth.request.ReactivateRequest;
import com.abonex.abonexbackend.dto.auth.request.RegisterRequest;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.entity.VerificationCode;
import com.abonex.abonexbackend.entity.enums.NotificationType;
import com.abonex.abonexbackend.entity.enums.VerificationCodeType;
import com.abonex.abonexbackend.repository.UserRepository;
import com.abonex.abonexbackend.repository.VerificationCodeRepository;
import com.abonex.abonexbackend.service.EmailService;
import com.abonex.abonexbackend.service.fcm.FCMService;
import com.abonex.abonexbackend.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationCodeRepository codeRepository;
    private final EmailService emailService;
    private final FCMService fCMService;

    public User getAuthenticatedUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kimliği doğrulanmış kullanıcı bulunamadı."));
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email adresi başka bir hesapta kullanılmaktadır!");
        }
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Telefon numarası başka bir hesapta kayıtlıdır!");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .profileImageUrl(request.getProfileImageUrl())
                .isEnabled(true)
                .build();
        User savedUser = userRepository.save(user);

        if (savedUser.getFcmToken() != null && !savedUser.getFcmToken().isBlank()) {
            fCMService.sendNotificationWithData(
                    savedUser.getFcmToken(),
                    "AboneX'e Hoş Geldin!",
                    "Merhaba " + savedUser.getFirstName() + "! Aboneliklerini yönetmeye başlamak için hazırız.",
                    Map.of("notificationType", NotificationType.WELCOME_MESSAGE.name())
                    );
        }

        String jwtToken = jwtService.generateToken(savedUser);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "HESAP_PASIF");
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Yanlış email veya parola.");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }


    @Transactional
    public void requestReactivationOtp(ReactivateRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Yanlış email veya parola."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Yanlış email veya parola.");
        }
        if (user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hesap zaten aktif durumda.");
        }

        String code = new DecimalFormat("0000").format(new Random().nextInt(9999));
        VerificationCode verificationCode = VerificationCode.builder()
                .user(user)
                .code(code)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .type(VerificationCodeType.ACCOUNT_REACTIVATION)
                .build();
        codeRepository.save(verificationCode);
        emailService.sendVerificationCode(user.getEmail(), code);
        if (user.getFcmToken() != null) {
            fCMService.sendNotificationWithData(
                    user.getFcmToken(),
                    "Doğrulama kodu gönderildi",
                    "Hesabınızı aktifleştirmek için kod e-posta adresinize gönderildi.",
                    Map.of("notificationType", NotificationType.ACCOUNT_REACTIVATION_CODE_SENT.name())
            );
        }
    }

    @Transactional
    public AuthResponse verifyReactivationOtp(VerifyCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geçersiz istek."));
        VerificationCode verificationCode = codeRepository
                .findFirstByUserAndTypeOrderByExpiryDateDesc(user, VerificationCodeType.ACCOUNT_REACTIVATION)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aktif bir kod isteği bulunamadı."));
        if (verificationCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doğrulama kodunun süresi dolmuş.");
        }
        if (!verificationCode.getCode().equals(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doğrulama kodu yanlış.");
        }

        user.setEnabled(true);
        userRepository.save(user);
        codeRepository.delete(verificationCode);

        if (user.getFcmToken() != null) {
            fCMService.sendNotificationWithData(
                    user.getFcmToken(),
                    "Hesabınız Aktif!",
                    "Abonex hesabınız başarıyla yeniden aktifleştirildi. Tekrardan hoş geldiniz!",
                    Map.of("notificationType", NotificationType.ACCOUNT_ACTIVATED.name())
            );
        }

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

}
