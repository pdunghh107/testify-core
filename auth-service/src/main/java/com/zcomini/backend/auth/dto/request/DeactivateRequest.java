package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.auth.validate.AuthValidateString;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeactivateRequest(
                @NotBlank(message = AuthValidateString.PASSWORD_REQUIRED) @Size(max = 72, message = AuthValidateString.PASSWORD_INVALID) String password,
                @Size(max = 255, message = AuthValidateString.DELETED_REASON_MAX_LENGTH) String deletedReason) {
}
