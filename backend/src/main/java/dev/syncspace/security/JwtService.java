package dev.syncspace.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long expiration;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-ms}") long expiration) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiration = expiration;
  }

  public String create(String email) {
    var issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    var expiresAt = issuedAt.plusMillis(expiration).truncatedTo(ChronoUnit.SECONDS);
    return Jwts.builder()
        .subject(email)
      .claim(Claims.ISSUED_AT, issuedAt.getEpochSecond())
      .claim(Claims.EXPIRATION, expiresAt.getEpochSecond())
        .signWith(key)
        .compact();
  }

  public String email(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
  }

  public boolean valid(String token) {
    try {
      email(token);
      return true;
    } catch (JwtException | IllegalArgumentException _) {
      return false;
    }
  }
}
