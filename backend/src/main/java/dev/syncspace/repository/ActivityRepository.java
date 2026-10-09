package dev.syncspace.repository;

import dev.syncspace.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
  List<Activity> findTop12ByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);
}
