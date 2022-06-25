package org.flywaydb.core.api.pattern;

import java.util.Arrays;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException;

public class ValidatePattern {
   private final String migrationType;
   private final String migrationState;
   private static final List<String> validMigrationTypes = Arrays.asList("*", "repeatable", "versioned");
   private static final List<String> validMigrationStates = Arrays.asList(
      "*",
      MigrationState.MISSING_SUCCESS.getDisplayName().toLowerCase(),
      MigrationState.PENDING.getDisplayName().toLowerCase(),
      MigrationState.IGNORED.getDisplayName().toLowerCase(),
      MigrationState.FUTURE_SUCCESS.getDisplayName().toLowerCase()
   );

   public static ValidatePattern fromPattern(String pattern) {
      if (pattern == null) {
         throw new FlywayException("Null pattern not allowed.");
      } else {
         String[] patternParts = pattern.split(":");
         if (patternParts.length != 2) {
            throw new FlywayException(
               "Invalid pattern '"
                  + pattern
                  + "'. Pattern must be of the form <migration_type>:<migration_state> See "
                  + "https://rd.gt/37m4hXD"
                  + " for full details"
            );
         } else {
            String migrationType = patternParts[0].trim().toLowerCase();
            String migrationState = patternParts[1].trim().toLowerCase();
            if (migrationType.equals("repeatable") || migrationType.equals("versioned")) {
               throw new FlywayTeamsUpgradeRequiredException("ignoreMigrationPattern with type '" + migrationType + "'");
            } else if (!validMigrationTypes.contains(migrationType)) {
               throw new FlywayException("Invalid migration type '" + patternParts[0] + "'. Valid types are: " + validMigrationTypes);
            } else if (!validMigrationStates.contains(migrationState)) {
               throw new FlywayException("Invalid migration state '" + patternParts[1] + "'. Valid states are: " + validMigrationStates);
            } else {
               return new ValidatePattern(migrationType, migrationState);
            }
         }
      }
   }

   public boolean matchesMigration(boolean isVersioned, MigrationState state) {
      if (!state.getDisplayName().equalsIgnoreCase(this.migrationState) && !this.migrationState.equals("*")) {
         return false;
      } else if (this.migrationType.equals("*")) {
         return true;
      } else if (isVersioned && this.migrationType.equalsIgnoreCase("versioned")) {
         return true;
      } else {
         return !isVersioned && this.migrationType.equalsIgnoreCase("repeatable");
      }
   }

   private ValidatePattern(String migrationType, String migrationState) {
      this.migrationType = migrationType;
      this.migrationState = migrationState;
   }
}
