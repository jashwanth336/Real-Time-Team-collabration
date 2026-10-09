package dev.syncspace.dto;

import dev.syncspace.model.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record TaskRequest(
    @NotBlank @Size(max = 160) String title,
    @Size(max = 2000) String description,
    TaskStatus status,
    Priority priority,
    LocalDate dueDate,
    Long assigneeId) {}
