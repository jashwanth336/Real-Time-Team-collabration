package dev.syncspace.repository;

import dev.syncspace.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
  List<Task> findByProjectIdOrderByUpdatedAtDesc(Long projectId);

  long countByProjectWorkspaceId(Long workspaceId);
}
