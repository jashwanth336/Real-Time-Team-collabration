package dev.syncspace.dto;

import jakarta.validation.constraints.*;

public record MemberRequest(@NotBlank @Email String email) {}
