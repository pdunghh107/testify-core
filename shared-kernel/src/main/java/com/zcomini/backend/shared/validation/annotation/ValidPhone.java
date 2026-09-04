package com.zcomini.backend.shared.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.zcomini.backend.shared.validation.validator.PhoneValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "Số điện thoại không hợp lệ";

    String locale() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
