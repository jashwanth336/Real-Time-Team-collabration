package dev.syncspace.controller;

import dev.syncspace.dto.UserSummary;
import dev.syncspace.service.AccessService;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final AccessService access;

  public UserController(AccessService a) {
    access = a;
  }

  @GetMapping("/me")
  public UserSummary me(Principal p) {
    return UserSummary.of(access.user(p.getName()));
  }
}
