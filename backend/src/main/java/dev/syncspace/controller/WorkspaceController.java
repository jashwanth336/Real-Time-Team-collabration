package dev.syncspace.controller;

import dev.syncspace.dto.*;
import dev.syncspace.model.*;
import dev.syncspace.repository.*;
import dev.syncspace.service.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {
  private final WorkspaceRepository workspaces;
  private final ProjectRepository projects;
  private final TaskRepository tasks;
  private final ActivityRepository activities;
  private final UserRepository users;
  private final AccessService access;
  private final ActivityService activity;

  public WorkspaceController(
      WorkspaceRepository w,
      ProjectRepository p,
      TaskRepository t,
      ActivityRepository a,
      UserRepository u,
      AccessService x,
      ActivityService y) {
    workspaces = w;
    projects = p;
    tasks = t;
    activities = a;
    users = u;
    access = x;
    activity = y;
  }

  @GetMapping
  public List<Workspace> all(Principal p) {
    return workspaces.findAccessible(access.user(p.getName()));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Workspace create(@Valid @RequestBody WorkspaceRequest r, Principal p) {
    var u = access.user(p.getName());
    var w = new Workspace();
    w.setName(r.name());
    w.setDescription(r.description());
    w.setOwner(u);
    w.getMembers().add(u);
    workspaces.save(w);
    activity.log(w, u, "created the workspace");
    return w;
  }

  @GetMapping("/{id}")
  public Map<String, Object> one(@PathVariable Long id, Principal p) {
    var w = access.workspace(id, p.getName());
    return Map.of(
        "workspace",
        w,
        "projects",
        projects.findByWorkspaceIdOrderByCreatedAtDesc(id),
        "activities",
        activities.findTop12ByWorkspaceIdOrderByCreatedAtDesc(id),
        "taskCount",
        tasks.countByProjectWorkspaceId(id));
  }

  @PutMapping("/{id}")
  public Workspace update(
      @PathVariable Long id, @Valid @RequestBody WorkspaceRequest r, Principal p) {
    var w = access.ownedWorkspace(id, p.getName());
    w.setName(r.name());
    w.setDescription(r.description());
    return workspaces.save(w);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, Principal p) {
    workspaces.delete(access.ownedWorkspace(id, p.getName()));
  }

  @PostMapping("/{id}/members")
  public Workspace addMember(
      @PathVariable Long id, @Valid @RequestBody MemberRequest r, Principal p) {
    var w = access.ownedWorkspace(id, p.getName());
    var u =
        users
            .findByEmail(r.email().toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("No account exists for that email"));
    if (w.getMembers().add(u)) {
      activity.log(
          w, access.user(p.getName()), "added " + u.getDisplayName() + " to the workspace");
      activity.notify(u, "You were added to workspace " + w.getName());
    }
    return workspaces.save(w);
  }
}
