package com.zcomini.backend.shared.validation.validator;

import java.util.regex.Pattern;

import com.zcomini.backend.shared.validation.annotation.ValidPassword;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{8,72}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Sẽ được xử lý bằng @NotBlank hoặc @NotNull
        }
        return PASSWORD_PATTERN.matcher(value).matches();
    }
}
