package dev.syncspace.controller;

import dev.syncspace.model.Notification;
import dev.syncspace.repository.NotificationRepository;
import dev.syncspace.service.AccessService;
import java.security.Principal;
import java.util.*;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
  private final NotificationRepository notifications;
  private final AccessService access;

  public NotificationController(NotificationRepository n, AccessService a) {
    notifications = n;
    access = a;
  }

  @GetMapping
  public List<Notification> all(Principal p) {
    return notifications.findTop30ByRecipientIdOrderByCreatedAtDesc(
        access.user(p.getName()).getId());
  }

  @PutMapping("/{id}/read")
  public Notification read(@PathVariable @NonNull Long id, Principal p) {
    var n =
        notifications
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
    if (!n.getRecipient().getId().equals(access.user(p.getName()).getId()))
      throw new org.springframework.security.access.AccessDeniedException("Not your notification");
    n.setRead(true);
    return notifications.save(n);
  }
}
