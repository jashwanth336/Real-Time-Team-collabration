package dev.syncspace.model;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "activities")
public class Activity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 300)
  private String message;

  @ManyToOne(optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Workspace workspace;

  @ManyToOne(optional = false)
  private AppUser actor;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public Long getId() {
    return id;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String v) {
    message = v;
  }

  public Workspace getWorkspace() {
    return workspace;
  }

  public void setWorkspace(Workspace v) {
    workspace = v;
  }

  public AppUser getActor() {
    return actor;
  }

  public void setActor(AppUser v) {
    actor = v;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
