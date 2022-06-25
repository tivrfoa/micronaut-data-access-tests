package org.flywaydb.core.api;

public class MigrationPattern {
   private final String migrationName;

   public boolean matches(MigrationVersion version, String description) {
      if (version != null) {
         String pattern = this.migrationName.replace("_", ".");
         return pattern.equals(version.toString());
      } else {
         String pattern = this.migrationName.replace("_", " ");
         return pattern.equals(description);
      }
   }

   public String toString() {
      return this.migrationName;
   }

   public int hashCode() {
      return this.migrationName.hashCode();
   }

   public MigrationPattern(String migrationName) {
      this.migrationName = migrationName;
   }
}
