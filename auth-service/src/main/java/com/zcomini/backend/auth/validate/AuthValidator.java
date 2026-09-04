package com.zcomini.backend.auth.validate;

import org.springframework.stereotype.Component;

import com.zcomini.backend.auth.dto.request.RegisterRequest;
import com.zcomini.backend.auth.exception.AuthException;
import com.zcomini.backend.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class AuthValidator {
    private final UserRepository userRepository;

    // [PUBLIC]
    public void registerValidation(RegisterRequest request) {
        checkConfirmPasswordMismatch(request.password(), request.confirmPassword());
        checkEmailTaken(request.email());
    }

    // [PRIVATE]
    private void checkConfirmPasswordMismatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw AuthException.passwordMismatch();
        }
    }

    private void checkEmailTaken(String email) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw AuthException.emailTaken();
        }
    }

}
