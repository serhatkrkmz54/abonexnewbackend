package com.abonex.abonexbackend.controller;

import com.abonex.abonexbackend.dto.auth.response.AuthResponse;
import com.abonex.abonexbackend.dto.auth.request.LoginRequest;
import com.abonex.abonexbackend.dto.auth.request.ReactivateRequest;
import com.abonex.abonexbackend.dto.auth.request.RegisterRequest;
import com.abonex.abonexbackend.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/reactivate-account")
    public ResponseEntity<AuthResponse> reactivate(@Valid @RequestBody ReactivateRequest request) {
        return ResponseEntity.ok(authService.reactivateAccount(request));
    }

}
