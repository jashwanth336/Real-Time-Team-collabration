package dev.syncspace.model;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "comments")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 2000)
  private String body;

  @ManyToOne(optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Task task;

  @ManyToOne(optional = false)
  private AppUser author;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public Long getId() {
    return id;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String v) {
    body = v;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task v) {
    task = v;
  }

  public AppUser getAuthor() {
    return author;
  }

  public void setAuthor(AppUser v) {
    author = v;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
