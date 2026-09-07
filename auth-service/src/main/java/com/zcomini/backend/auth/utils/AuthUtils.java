package com.zcomini.backend.auth.utils;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zcomini.backend.auth.entity.UserEntity;
import com.zcomini.backend.auth.exception.AuthException;
import com.zcomini.backend.auth.exception.UserException;
import com.zcomini.backend.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class AuthUtils {

    private final UserRepository userRepository;

    // [PUBLIC]
    // [LOGIN]
    public UserEntity findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim()).orElseThrow(AuthException::credentialsInvalid);
    }

    // [COMMON]
    public UserEntity findUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(UserException::userNotFound);
    }
}
