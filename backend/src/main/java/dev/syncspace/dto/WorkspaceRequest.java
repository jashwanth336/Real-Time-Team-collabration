package dev.syncspace.dto;

import jakarta.validation.constraints.*;

public record WorkspaceRequest(
    @NotBlank @Size(max = 100) String name, @Size(max = 500) String description) {}
