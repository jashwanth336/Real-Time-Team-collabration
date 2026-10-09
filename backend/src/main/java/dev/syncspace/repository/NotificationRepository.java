package dev.syncspace.repository;

import dev.syncspace.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findTop30ByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}
