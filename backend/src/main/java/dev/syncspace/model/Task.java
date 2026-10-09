package dev.syncspace.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "tasks")
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskStatus status = TaskStatus.TODO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Priority priority = Priority.MEDIUM;

  private LocalDate dueDate;

  @ManyToOne(optional = false)
  @JoinColumn(name = "project_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Project project;

  @ManyToOne
  @JoinColumn(name = "assignee_id")
  private AppUser assignee;

  @ManyToOne(optional = false)
  @JoinColumn(name = "reporter_id")
  private AppUser reporter;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String v) {
    title = v;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String v) {
    description = v;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public void setStatus(TaskStatus v) {
    status = v;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority v) {
    priority = v;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate v) {
    dueDate = v;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project v) {
    project = v;
  }

  public AppUser getAssignee() {
    return assignee;
  }

  public void setAssignee(AppUser v) {
    assignee = v;
  }

  public AppUser getReporter() {
    return reporter;
  }

  public void setReporter(AppUser v) {
    reporter = v;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
