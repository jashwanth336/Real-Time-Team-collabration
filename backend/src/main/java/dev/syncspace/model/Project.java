package dev.syncspace.model;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "projects")
public class Project {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProjectStatus status = ProjectStatus.ACTIVE;

  @ManyToOne(optional = false)
  @JoinColumn(name = "workspace_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Workspace workspace;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String v) {
    name = v;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String v) {
    description = v;
  }

  public ProjectStatus getStatus() {
    return status;
  }

  public void setStatus(ProjectStatus v) {
    status = v;
  }

  public Workspace getWorkspace() {
    return workspace;
  }

  public void setWorkspace(Workspace v) {
    workspace = v;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
