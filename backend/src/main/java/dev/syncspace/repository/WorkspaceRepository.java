package dev.syncspace.repository;

import dev.syncspace.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
  @Query(
      "select distinct w from Workspace w left join w.members m where w.owner = :user or m = :user order by w.createdAt desc")
  List<Workspace> findAccessible(AppUser user);
}
