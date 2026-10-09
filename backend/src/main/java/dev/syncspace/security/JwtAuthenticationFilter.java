package dev.syncspace.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final UserDetailsService users;

  public JwtAuthenticationFilter(JwtService jwt, UserDetailsService users) {
    this.jwt = jwt;
    this.users = users;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest r,
      @NonNull HttpServletResponse s,
      @NonNull FilterChain c)
      throws ServletException, IOException {
    String h = r.getHeader("Authorization");
    if (h != null && h.startsWith("Bearer ")) {
      String t = h.substring(7);
      if (jwt.valid(t) && SecurityContextHolder.getContext().getAuthentication() == null) {
        var u = users.loadUserByUsername(jwt.email(t));
        var a = new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
        a.setDetails(new WebAuthenticationDetailsSource().buildDetails(r));
        SecurityContextHolder.getContext().setAuthentication(a);
      }
    }
    c.doFilter(r, s);
  }
}
