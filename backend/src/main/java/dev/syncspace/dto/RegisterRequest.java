package dev.syncspace.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank
        @Pattern(
            regexp = "^[a-zA-Z0-9_.-]{3,40}$",
            message = "must be 3-40 letters, numbers, dots, hyphens or underscores")
        String username,
    @NotBlank @Size(min = 2, max = 80) String displayName,
    @NotBlank @Size(min = 8, max = 100) String password) {}
