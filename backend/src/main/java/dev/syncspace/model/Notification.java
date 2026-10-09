package dev.syncspace.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 300)
  private String message;

  @ManyToOne(optional = false)
  private AppUser recipient;

  @Column(nullable = false)
  private boolean read = false;

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

  public AppUser getRecipient() {
    return recipient;
  }

  public void setRecipient(AppUser v) {
    recipient = v;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean v) {
    read = v;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
