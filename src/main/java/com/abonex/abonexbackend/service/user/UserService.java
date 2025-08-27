package com.abonex.abonexbackend.service.user;

import com.abonex.abonexbackend.dto.auth.UserProfileUpdateRequest;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.repository.UserRepository;
import com.abonex.abonexbackend.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuthService authService;

    public User getAuthenticatedUserProfile() {
        return authService.getAuthenticatedUser();
    }

    public User updateUserProfile(UserProfileUpdateRequest request) {
        User userToUpdate = authService.getAuthenticatedUser();

        if (request.getFirstName() != null) {
            userToUpdate.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            userToUpdate.setLastName(request.getLastName());
        }
        if (!userToUpdate.getEmail().equals(request.getEmail())) {
            Optional<User> userWithNewEmail = userRepository.findByEmail(request.getEmail());
            if (userWithNewEmail.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Email adresi başka bir hesapta kullanılmaktadır!");
            }
        }
        if (!userToUpdate.getPhoneNumber().equals(request.getPhoneNumber())) {
            Optional<User> userWithNewPhone = userRepository.findByPhoneNumber(request.getPhoneNumber());
            if (userWithNewPhone.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Telefon numarası başka bir hesapta kayıtlıdır!");
            }
        }

        if (request.getDateOfBirth() != null) {
            userToUpdate.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            userToUpdate.setGender(request.getGender());
        }
        if (request.getProfileImageUrl() != null) {
            userToUpdate.setProfileImageUrl(request.getProfileImageUrl());
        }
        return userRepository.save(userToUpdate);
    }

    public void deactiveAccount(){
        User userToDelete = authService.getAuthenticatedUser();
        userToDelete.setEnabled(false);
        userRepository.save(userToDelete);
    }
}
