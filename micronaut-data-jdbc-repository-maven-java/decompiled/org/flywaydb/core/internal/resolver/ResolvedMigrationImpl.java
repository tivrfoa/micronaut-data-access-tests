package org.flywaydb.core.internal.resolver;

import java.util.Objects;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.resolver.ResolvedMigration;

public class ResolvedMigrationImpl implements ResolvedMigration {
   private final String script;
   private final Integer equivalentChecksum;
   private final Integer checksum;
   private final MigrationVersion version;
   private final String description;
   private final MigrationType type;
   private final String physicalLocation;
   private final MigrationExecutor executor;

   public ResolvedMigrationImpl(
      MigrationVersion version,
      String description,
      String script,
      Integer checksum,
      Integer equivalentChecksum,
      MigrationType type,
      String physicalLocation,
      MigrationExecutor executor
   ) {
      this.version = version;
      this.description = description;
      this.script = script;
      this.checksum = checksum;
      this.equivalentChecksum = equivalentChecksum;
      this.type = type;
      this.physicalLocation = physicalLocation;
      this.executor = executor;
   }

   public void validate() {
   }

   @Override
   public Integer getChecksum() {
      return this.checksum == null ? this.equivalentChecksum : this.checksum;
   }

   public int compareTo(ResolvedMigrationImpl o) {
      return this.version.compareTo(o.version);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ResolvedMigrationImpl migration = (ResolvedMigrationImpl)o;
         if (this.checksum != null) {
            if (!this.checksum.equals(migration.checksum)) {
               return false;
            }
         } else if (migration.checksum != null) {
            return false;
         }

         if (this.equivalentChecksum != null) {
            if (!this.equivalentChecksum.equals(migration.equivalentChecksum)) {
               return false;
            }
         } else if (migration.equivalentChecksum != null) {
            return false;
         }

         if (this.description != null) {
            if (!this.description.equals(migration.description)) {
               return false;
            }
         } else if (migration.description != null) {
            return false;
         }

         if (this.script != null) {
            if (!this.script.equals(migration.script)) {
               return false;
            }
         } else if (migration.script != null) {
            return false;
         }

         return this.type != migration.type ? false : Objects.equals(this.version, migration.version);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.version != null ? this.version.hashCode() : 0;
      result = 31 * result + (this.description != null ? this.description.hashCode() : 0);
      result = 31 * result + (this.script != null ? this.script.hashCode() : 0);
      result = 31 * result + (this.checksum != null ? this.checksum.hashCode() : 0);
      result = 31 * result + (this.equivalentChecksum != null ? this.equivalentChecksum.hashCode() : 0);
      return 31 * result + this.type.hashCode();
   }

   public String toString() {
      return "ResolvedMigrationImpl{version="
         + this.version
         + ", description='"
         + this.description
         + '\''
         + ", script='"
         + this.script
         + '\''
         + ", checksum="
         + this.getChecksum()
         + ", type="
         + this.type
         + ", physicalLocation='"
         + this.physicalLocation
         + '\''
         + ", executor="
         + this.executor
         + '}';
   }

   @Override
   public boolean checksumMatches(Integer checksum) {
      return Objects.equals(checksum, this.checksum) || Objects.equals(checksum, this.equivalentChecksum) && this.equivalentChecksum != null;
   }

   @Override
   public boolean checksumMatchesWithoutBeingIdentical(Integer checksum) {
      return Objects.equals(checksum, this.equivalentChecksum) && !Objects.equals(checksum, this.checksum);
   }

   @Override
   public String getScript() {
      return this.script;
   }

   @Override
   public MigrationVersion getVersion() {
      return this.version;
   }

   @Override
   public String getDescription() {
      return this.description;
   }

   @Override
   public MigrationType getType() {
      return this.type;
   }

   @Override
   public String getPhysicalLocation() {
      return this.physicalLocation;
   }

   @Override
   public MigrationExecutor getExecutor() {
      return this.executor;
   }
}
