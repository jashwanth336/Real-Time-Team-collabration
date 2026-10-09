package dev.syncspace.controller;

import dev.syncspace.dto.*;
import dev.syncspace.model.*;
import dev.syncspace.repository.UserRepository;
import dev.syncspace.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final AuthenticationManager auth;
  private final JwtService jwt;

  public AuthController(
      UserRepository u, PasswordEncoder e, AuthenticationManager a, JwtService j) {
    users = u;
    encoder = e;
    auth = a;
    jwt = j;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
    if (users.existsByEmail(r.email()))
      throw new IllegalArgumentException("Email is already registered");
    if (users.existsByUsername(r.username()))
      throw new IllegalArgumentException("Username is already taken");
    var u = new AppUser();
    u.setEmail(r.email());
    u.setUsername(r.username());
    u.setDisplayName(r.displayName());
    u.setPassword(encoder.encode(r.password()));
    users.save(u);
    return AuthResponse.of(jwt.create(u.getEmail()), u);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody AuthRequest r) {
    try {
      auth.authenticate(
          new UsernamePasswordAuthenticationToken(r.email().toLowerCase(), r.password()));
    } catch (AuthenticationException e) {
      throw new IllegalArgumentException("Invalid email or password");
    }
    var u = users.findByEmail(r.email().toLowerCase()).orElseThrow();
    return AuthResponse.of(jwt.create(u.getEmail()), u);
  }
}
