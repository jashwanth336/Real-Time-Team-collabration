package dev.syncspace.dto;

import jakarta.validation.constraints.*;

public record AuthRequest(
    @NotBlank @Email String email, @NotBlank @Size(min = 8, max = 100) String password) {}
