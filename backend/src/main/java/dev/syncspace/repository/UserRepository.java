package dev.syncspace.repository;

import dev.syncspace.model.AppUser;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
