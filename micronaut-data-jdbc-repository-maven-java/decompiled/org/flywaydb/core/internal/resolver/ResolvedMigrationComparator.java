package org.flywaydb.core.internal.resolver;

import java.util.Comparator;
import org.flywaydb.core.api.resolver.ResolvedMigration;

public class ResolvedMigrationComparator implements Comparator<ResolvedMigration> {
   public int compare(ResolvedMigration o1, ResolvedMigration o2) {
      if (o1.getVersion() != null && o2.getVersion() != null) {
         return o1.getVersion().compareTo(o2.getVersion());
      } else if (o1.getVersion() != null) {
         return -1;
      } else {
         return o2.getVersion() != null ? 1 : o1.getDescription().compareTo(o2.getDescription());
      }
   }
}
