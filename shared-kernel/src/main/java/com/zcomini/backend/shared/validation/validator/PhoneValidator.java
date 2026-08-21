package com.zcomini.backend.shared.validation.validator;

import com.zcomini.backend.shared.validation.annotation.ValidPhone;
import com.zcomini.backend.shared.validation.config.PhoneValidationProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private final PhoneValidationProperties properties;

    private String locale;

    // Cache the compiled patterns for performance
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    PhoneValidator(PhoneValidationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        this.locale = constraintAnnotation.locale();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null values are handled by @NotNull
        if (value == null) {
            return true;
        }

        // Determine which locale to use
        String targetLocale = StringUtils.hasText(this.locale) ? this.locale : properties.getDefaultLocale();
        if (!StringUtils.hasText(targetLocale)) {
            // No configuration available, default to pass or fail? We fail safely if it's
            // misconfigured.
            return false;
        }

        // Get the regex rule for the target locale
        String regex = properties.getRules().get(targetLocale);
        if (!StringUtils.hasText(regex)) {
            return false;
        }

        // Compile and test pattern (use cache for performance)
        Pattern pattern = PATTERN_CACHE.computeIfAbsent(regex, Pattern::compile);
        return pattern.matcher(value).matches();
    }
}
