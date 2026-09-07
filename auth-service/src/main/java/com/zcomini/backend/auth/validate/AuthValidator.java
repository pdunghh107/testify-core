package com.zcomini.backend.auth.validate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.zcomini.backend.auth.dto.request.RegisterRequest;
import com.zcomini.backend.auth.entity.UserEntity;
import com.zcomini.backend.auth.exception.AuthException;
import com.zcomini.backend.auth.exception.UserException;
import com.zcomini.backend.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class AuthValidator {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // [PUBLIC]
    // [REGISTER]
    public void registerValidation(RegisterRequest request) {
        checkConfirmPasswordMismatch(request.password(), request.confirmPassword());
        checkEmailTaken(request.email());
    }

    // [LOGIN]
    public void checkUserInactive(UserEntity user) {
        if (!user.isActive()) {
            throw UserException.userInactive();
        }
    }

    public void checkPasswordMatch(String password, String passwordHash) {
        if (!passwordEncoder.matches(password, passwordHash)) {
            throw AuthException.credentialsInvalid();
        }
    }

    public void checkNewPasswordDifferentFromOldPassword(String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            throw AuthException.newPasswordMustBeDifferent();
        }
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
