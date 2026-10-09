package dev.syncspace.dto;

import dev.syncspace.model.AppUser;

public record UserSummary(Long id, String email, String username, String displayName, String role) {
  public static UserSummary of(AppUser u) {
    return new UserSummary(
        u.getId(), u.getEmail(), u.getUsername(), u.getDisplayName(), u.getRole().name());
  }
}
