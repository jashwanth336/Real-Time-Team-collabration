package dev.syncspace.service;

import dev.syncspace.model.*;
import dev.syncspace.repository.*;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {
  private final ActivityRepository activities;
  private final NotificationRepository notifications;

  public ActivityService(ActivityRepository a, NotificationRepository n) {
    activities = a;
    notifications = n;
  }

  public void log(Workspace workspace, AppUser actor, String message) {
    var a = new Activity();
    a.setWorkspace(workspace);
    a.setActor(actor);
    a.setMessage(message);
    activities.save(a);
  }

  public void notify(AppUser recipient, String message) {
    var n = new Notification();
    n.setRecipient(recipient);
    n.setMessage(message);
    notifications.save(n);
  }
}
