package dev.syncspace.dto;

import dev.syncspace.model.AppUser;

public record AuthResponse(String token, UserSummary user) {
  public static AuthResponse of(String token, AppUser u) {
    return new AuthResponse(token, UserSummary.of(u));
  }
}
