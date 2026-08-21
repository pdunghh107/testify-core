package com.zcomini.backend.testify.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFolderRequest(
        @NotBlank(message = "{folder.name.required}") @Size(min = 2, max = 100, message = "{folder.name.invalid}") String name,
        UUID parentFolderId) {
}
