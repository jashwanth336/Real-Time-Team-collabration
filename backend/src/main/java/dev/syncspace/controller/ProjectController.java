package dev.syncspace.controller;

import dev.syncspace.dto.ProjectRequest;
import dev.syncspace.model.Project;
import dev.syncspace.model.ProjectStatus;
import dev.syncspace.repository.ProjectRepository;
import dev.syncspace.repository.TaskRepository;
import dev.syncspace.service.AccessService;
import dev.syncspace.service.ActivityService;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProjectController {

    private final ProjectRepository projects;
    private final TaskRepository tasks;
    private final AccessService access;
    private final ActivityService activity;

    public ProjectController(
            ProjectRepository projects,
            TaskRepository tasks,
            AccessService access,
            ActivityService activity) {

        this.projects = projects;
        this.tasks = tasks;
        this.access = access;
        this.activity = activity;
    }

    // Get all projects in a workspace
    @GetMapping("/workspaces/{workspaceId}/projects")
    public List<Project> all(
            @PathVariable Long workspaceId,
            Principal principal) {

        access.workspace(workspaceId, principal.getName());

        return projects.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId);
    }

    // Create a new project
    @PostMapping("/workspaces/{workspaceId}/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public Project create(
            @PathVariable Long workspaceId,
            @Valid @RequestBody ProjectRequest request,
            Principal principal) {

        var workspace = access.workspace(
                workspaceId,
                principal.getName()
        );

        var project = new Project();

        project.setName(request.name());
        project.setDescription(request.description());

        project.setStatus(
                request.status() == null
                        ? ProjectStatus.ACTIVE
                        : request.status()
        );

        project.setWorkspace(workspace);

        projects.save(project);

        activity.log(
                workspace,
                access.user(principal.getName()),
                "created project " + project.getName()
        );

        return project;
    }

    // Get a single project with its tasks
    @GetMapping("/projects/{id}")
    public Map<String, Object> one(
            @PathVariable Long id,
            Principal principal) {

        var project = access.project(
                id,
                principal.getName()
        );

        return Map.of(
                "project", project,
                "tasks",
                tasks.findByProjectIdOrderByUpdatedAtDesc(id)
        );
    }

    // Update a project
    @PutMapping("/projects/{id}")
    public Project update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            Principal principal) {

        var project = access.project(
                id,
                principal.getName()
        );

        project.setName(request.name());
        project.setDescription(request.description());

        if (request.status() != null) {
            project.setStatus(request.status());
        }

        return projects.save(project);
    }

    // Delete a project
    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Principal principal) {

        var project = access.project(
                id,
                principal.getName()
        );

        projects.delete(project);
    }
}