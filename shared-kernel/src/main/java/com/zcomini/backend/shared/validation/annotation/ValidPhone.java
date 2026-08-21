package com.zcomini.backend.shared.validation.annotation;

import com.zcomini.backend.shared.validation.validator.PhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "{phone.invalid}";

    String locale() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
