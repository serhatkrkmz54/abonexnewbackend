package com.abonex.abonexbackend.controller;

import com.abonex.abonexbackend.dto.auth.request.UserProfileUpdateRequest;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> getMyProfile() {
        return ResponseEntity.ok(userService.getAuthenticatedUserProfile());
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(request));
    }

    @PatchMapping("/deactive-account")
    public ResponseEntity<Void> deactiveMyAccount() {
        userService.deactiveAccount();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
