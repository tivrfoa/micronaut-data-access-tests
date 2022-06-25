package org.flywaydb.core.internal.schemahistory;

import java.util.Date;
import java.util.Objects;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;

public class AppliedMigration implements Comparable<AppliedMigration> {
   private final int installedRank;
   private final MigrationVersion version;
   private final String description;
   private final MigrationType type;
   private final String script;
   private final Integer checksum;
   private final Date installedOn;
   private final String installedBy;
   private final int executionTime;
   private final boolean success;

   public int getInstalledRank() {
      return this.installedRank;
   }

   public MigrationVersion getVersion() {
      return this.version;
   }

   public String getDescription() {
      return this.description;
   }

   public MigrationType getType() {
      return this.type;
   }

   public String getScript() {
      return this.script;
   }

   public Integer getChecksum() {
      return this.checksum;
   }

   public Date getInstalledOn() {
      return this.installedOn;
   }

   public String getInstalledBy() {
      return this.installedBy;
   }

   public int getExecutionTime() {
      return this.executionTime;
   }

   public boolean isSuccess() {
      return this.success;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AppliedMigration that = (AppliedMigration)o;
         if (this.executionTime != that.executionTime) {
            return false;
         } else if (this.installedRank != that.installedRank) {
            return false;
         } else if (this.success != that.success) {
            return false;
         } else {
            if (this.checksum != null) {
               if (!this.checksum.equals(that.checksum)) {
                  return false;
               }
            } else if (that.checksum != null) {
               return false;
            }

            if (!this.description.equals(that.description)) {
               return false;
            } else {
               if (this.installedBy != null) {
                  if (!this.installedBy.equals(that.installedBy)) {
                     return false;
                  }
               } else if (that.installedBy != null) {
                  return false;
               }

               if (this.installedOn != null) {
                  if (!this.installedOn.equals(that.installedOn)) {
                     return false;
                  }
               } else if (that.installedOn != null) {
                  return false;
               }

               if (!this.script.equals(that.script)) {
                  return false;
               } else {
                  return this.type != that.type ? false : Objects.equals(this.version, that.version);
               }
            }
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.installedRank;
      result = 31 * result + (this.version != null ? this.version.hashCode() : 0);
      result = 31 * result + this.description.hashCode();
      result = 31 * result + this.type.hashCode();
      result = 31 * result + this.script.hashCode();
      result = 31 * result + (this.checksum != null ? this.checksum.hashCode() : 0);
      result = 31 * result + (this.installedOn != null ? this.installedOn.hashCode() : 0);
      result = 31 * result + (this.installedBy != null ? this.installedBy.hashCode() : 0);
      result = 31 * result + this.executionTime;
      return 31 * result + (this.success ? 1 : 0);
   }

   public int compareTo(AppliedMigration o) {
      return this.installedRank - o.installedRank;
   }

   public AppliedMigration(
      int installedRank,
      MigrationVersion version,
      String description,
      MigrationType type,
      String script,
      Integer checksum,
      Date installedOn,
      String installedBy,
      int executionTime,
      boolean success
   ) {
      this.installedRank = installedRank;
      this.version = version;
      this.description = description;
      this.type = type;
      this.script = script;
      this.checksum = checksum;
      this.installedOn = installedOn;
      this.installedBy = installedBy;
      this.executionTime = executionTime;
      this.success = success;
   }
}
