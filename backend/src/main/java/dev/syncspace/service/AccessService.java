package dev.syncspace.service;

import dev.syncspace.model.*;
import dev.syncspace.repository.*;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccessService {
  private final UserRepository users;
  private final WorkspaceRepository workspaces;
  private final ProjectRepository projects;
  private final TaskRepository tasks;

  public AccessService(
      UserRepository u, WorkspaceRepository w, ProjectRepository p, TaskRepository t) {
    users = u;
    workspaces = w;
    projects = p;
    tasks = t;
  }

  public AppUser user(String email) {
    return users
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  public @NonNull Workspace workspace(@NonNull Long id, String email) {
    var w =
        workspaces
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
    if (!isMember(w, user(email)))
      throw new AccessDeniedException("You do not have access to this workspace");
    return w;
  }

  public @NonNull Workspace ownedWorkspace(@NonNull Long id, String email) {
    var w = workspace(id, email);
    if (!w.getOwner().getId().equals(user(email).getId()))
      throw new AccessDeniedException("Only the workspace owner can do that");
    return w;
  }

  public @NonNull Project project(@NonNull Long id, String email) {
    var p =
        projects.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found"));
    workspace(p.getWorkspace().getId(), email);
    return p;
  }

  public @NonNull Task task(@NonNull Long id, String email) {
    var t = tasks.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found"));
    project(t.getProject().getId(), email);
    return t;
  }

  public boolean isMember(Workspace w, AppUser u) {
    return w.getOwner().getId().equals(u.getId())
        || w.getMembers().stream().anyMatch(m -> m.getId().equals(u.getId()));
  }
}
