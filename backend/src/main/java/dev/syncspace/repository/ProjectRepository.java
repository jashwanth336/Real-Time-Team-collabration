package dev.syncspace.repository;

import dev.syncspace.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
  List<Project> findByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);
}
