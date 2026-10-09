package dev.syncspace.controller;

import dev.syncspace.dto.CommentRequest;
import dev.syncspace.dto.TaskRequest;
import dev.syncspace.model.Comment;
import dev.syncspace.model.Priority;
import dev.syncspace.model.Task;
import dev.syncspace.model.TaskStatus;
import dev.syncspace.model.Workspace;
import dev.syncspace.repository.CommentRepository;
import dev.syncspace.repository.TaskRepository;
import dev.syncspace.repository.UserRepository;
import dev.syncspace.service.AccessService;
import dev.syncspace.service.ActivityService;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public class TaskController {

    private final TaskRepository tasks;
    private final CommentRepository commentRepository;
    private final UserRepository users;
    private final AccessService access;
    private final ActivityService activity;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TaskController(
            TaskRepository tasks,
            CommentRepository commentRepository,
            UserRepository users,
            AccessService access,
            ActivityService activity) {

        this.tasks = tasks;
        this.commentRepository = commentRepository;
        this.users = users;
        this.access = access;
        this.activity = activity;
    }

    // =========================================================
    // GET ALL TASKS FOR A PROJECT
    // =========================================================

    @GetMapping("/projects/{projectId}/tasks")
    public List<Task> getAllTasks(
            @PathVariable Long projectId,
            Principal principal) {

        // Check whether current user can access the project
        access.project(
                projectId,
                principal.getName()
        );

        return tasks.findByProjectIdOrderByUpdatedAtDesc(projectId);
    }

    // =========================================================
    // CREATE NEW TASK
    // =========================================================

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request,
            Principal principal) {

        // Get project and verify access
        var project = access.project(
                projectId,
                principal.getName()
        );

        // Get logged-in user
        var actor = access.user(
                principal.getName()
        );

        // Create task
        var task = new Task();

        task.setProject(project);
        task.setReporter(actor);

        // Apply request data
        applyTaskData(
                task,
                request,
                project.getWorkspace()
        );

        // Save task
        tasks.save(task);

        // Record activity
        activity.log(
                project.getWorkspace(),
                actor,
                "created task " + task.getTitle()
        );

        // Notify assignee
        if (task.getAssignee() != null
                && !task.getAssignee().getId().equals(actor.getId())) {

            activity.notify(
                    task.getAssignee(),
                    "You were assigned task " + task.getTitle()
            );
        }

        return task;
    }

    // =========================================================
    // GET SINGLE TASK
    // =========================================================

    @GetMapping("/tasks/{id}")
    public Map<String, Object> getTask(
            @PathVariable Long id,
            Principal principal) {

        // Check access
        var task = access.task(
                id,
                principal.getName()
        );

        // Return task and comments
        return Map.of(
                "task",
                task,
                "comments",
                commentRepository.findByTaskIdOrderByCreatedAtAsc(id)
        );
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================

    @PutMapping("/tasks/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            Principal principal) {

        // Get task
        var task = access.task(
                id,
                principal.getName()
        );

        // Get logged-in user
        var actor = access.user(
                principal.getName()
        );

        // Store old status
        TaskStatus oldStatus = task.getStatus();

        // Apply updated data
        applyTaskData(
                task,
                request,
                task.getProject().getWorkspace()
        );

        // Save changes
        tasks.save(task);

        // Determine activity message
        String action;

        if (oldStatus != task.getStatus()) {
            action = "moved task ";
        } else {
            action = "updated task ";
        }

        // Log activity
        activity.log(
                task.getProject().getWorkspace(),
                actor,
                action + task.getTitle()
        );

        // Notify assignee
        if (task.getAssignee() != null
                && !task.getAssignee().getId().equals(actor.getId())) {

            activity.notify(
                    task.getAssignee(),
                    "Task updated: " + task.getTitle()
            );
        }

        return task;
    }

    // =========================================================
    // DELETE TASK
    // =========================================================

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable Long id,
            Principal principal) {

        // Get task and verify access
        var task = access.task(
                id,
                principal.getName()
        );

        // Get logged-in user
        var actor = access.user(
                principal.getName()
        );

        // Log deletion
        activity.log(
                task.getProject().getWorkspace(),
                actor,
                "deleted task " + task.getTitle()
        );

        // Delete task
        tasks.delete(task);
    }

    // =========================================================
    // GET COMMENTS FOR TASK
    // =========================================================

    @GetMapping("/tasks/{id}/comments")
    public List<Comment> getComments(
            @PathVariable Long id,
            Principal principal) {

        // Check whether user can access task
        access.task(
                id,
                principal.getName()
        );

        return commentRepository
                .findByTaskIdOrderByCreatedAtAsc(id);
    }

    // =========================================================
    // ADD COMMENT TO TASK
    // =========================================================

    @PostMapping("/tasks/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            Principal principal) {

        // Get task
        var task = access.task(
                id,
                principal.getName()
        );

        // Get logged-in user
        var actor = access.user(
                principal.getName()
        );

        // Create comment
        var comment = new Comment();

        comment.setTask(task);
        comment.setAuthor(actor);
        comment.setBody(request.body());

        // Save comment
        commentRepository.save(comment);

        // Log activity
        activity.log(
                task.getProject().getWorkspace(),
                actor,
                "commented on " + task.getTitle()
        );

        // Notify task reporter
        if (!task.getReporter().getId().equals(actor.getId())) {

            activity.notify(
                    task.getReporter(),
                    actor.getDisplayName()
                            + " commented on "
                            + task.getTitle()
            );
        }

        return comment;
    }

    // =========================================================
    // APPLY TASK REQUEST DATA
    // =========================================================

    private void applyTaskData(
            Task task,
            TaskRequest request,
            Workspace workspace) {

        // Task title
        task.setTitle(
                request.title()
        );

        // Task description
        task.setDescription(
                request.description()
        );

        // Task status
        task.setStatus(
                request.status() == null
                        ? TaskStatus.TODO
                        : request.status()
        );

        // Task priority
        task.setPriority(
                request.priority() == null
                        ? Priority.MEDIUM
                        : request.priority()
        );

        // Due date
        task.setDueDate(
                request.dueDate()
        );

        // =====================================================
        // ASSIGNEE
        // =====================================================

        if (request.assigneeId() == null) {

            // No assignee
            task.setAssignee(null);

            return;
        }

        // Find assignee
        var assignee = users
                .findById(
                        Objects.requireNonNull(
                                request.assigneeId()
                        )
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Assignee not found"
                        )
                );

        // Verify workspace membership
        if (!access.isMember(workspace, assignee)) {

            throw new IllegalArgumentException(
                    "Assignee must be a workspace member"
            );
        }

        // Set assignee
        task.setAssignee(assignee);
    }
}