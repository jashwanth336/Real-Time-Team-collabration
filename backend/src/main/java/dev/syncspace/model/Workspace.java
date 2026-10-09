package dev.syncspace.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "workspaces")
public class Workspace {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @ManyToOne(optional = false)
  @JoinColumn(name = "owner_id")
  private AppUser owner;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "workspace_members",
      joinColumns = @JoinColumn(name = "workspace_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private Set<AppUser> members = new HashSet<>();

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

  public AppUser getOwner() {
    return owner;
  }

  public void setOwner(AppUser v) {
    owner = v;
  }

  public Set<AppUser> getMembers() {
    return members;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
