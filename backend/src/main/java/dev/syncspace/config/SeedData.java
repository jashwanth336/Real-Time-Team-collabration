package dev.syncspace.config;

import dev.syncspace.model.AppUser;
import dev.syncspace.model.Priority;
import dev.syncspace.model.Project;
import dev.syncspace.model.Task;
import dev.syncspace.model.TaskStatus;
import dev.syncspace.model.Workspace;
import dev.syncspace.repository.ProjectRepository;
import dev.syncspace.repository.TaskRepository;
import dev.syncspace.repository.UserRepository;
import dev.syncspace.repository.WorkspaceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedData {

  @Bean
  CommandLineRunner seed(
      UserRepository users,
      WorkspaceRepository workspaces,
      ProjectRepository projects,
      TaskRepository tasks,
      PasswordEncoder encoder) {
    return args -> {
      if (users.existsByEmail("demo@syncspace.dev")) {
        return;
      }

      var user = new AppUser();
      user.setEmail("demo@syncspace.dev");
      user.setUsername("demo");
      user.setDisplayName("Demo User");
      user.setPassword(encoder.encode("Password123!"));
      users.save(user);

      var workspace = new Workspace();
      workspace.setName("Product Launch");
      workspace.setDescription("A sample workspace to explore SyncSpace.");
      workspace.setOwner(user);
      workspace.getMembers().add(user);
      workspaces.save(workspace);

      var project = new Project();
      project.setName("Marketing Website");
      project.setDescription("Plan and deliver the launch website.");
      project.setWorkspace(workspace);
      projects.save(project);

      createTask(tasks, project, user, "Create hero section", TaskStatus.DONE, Priority.HIGH);
      createTask(
          tasks, project, user, "Write product copy", TaskStatus.IN_PROGRESS, Priority.MEDIUM);
      createTask(tasks, project, user, "Review analytics plan", TaskStatus.TODO, Priority.LOW);
    };
  }

  private void createTask(
      TaskRepository tasks,
      Project project,
      AppUser user,
      String title,
      TaskStatus status,
      Priority priority) {
    var task = new Task();
    task.setProject(project);
    task.setReporter(user);
    task.setAssignee(user);
    task.setTitle(title);
    task.setStatus(status);
    task.setPriority(priority);
    tasks.save(task);
  }
}
