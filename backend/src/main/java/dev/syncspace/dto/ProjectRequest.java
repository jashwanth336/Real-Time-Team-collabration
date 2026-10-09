package dev.syncspace.dto;

import dev.syncspace.model.ProjectStatus;
import jakarta.validation.constraints.*;

public record ProjectRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 1000) String description,
    ProjectStatus status) {}
