package org.flywaydb.core.api.migration;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.util.Pair;

public abstract class BaseJavaMigration implements JavaMigration {
   private final MigrationVersion version;
   private final String description;

   public BaseJavaMigration() {
      String shortName = this.getClass().getSimpleName();
      String prefix = null;
      boolean repeatable = shortName.startsWith("R");
      if (shortName.startsWith("V") || repeatable) {
         prefix = shortName.substring(0, 1);
      }

      if (prefix == null) {
         throw new FlywayException(
            "Invalid Java-based migration class name: "
               + this.getClass().getName()
               + " => ensure it starts with V, R or implement org.flywaydb.core.api.migration.JavaMigration directly for non-default naming"
         );
      } else {
         Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription(shortName, prefix, "__", new String[]{""}, repeatable);
         this.version = info.getLeft();
         this.description = (String)info.getRight();
      }
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
   public Integer getChecksum() {
      return null;
   }

   @Override
   public boolean isUndo() {
      return false;
   }

   @Override
   public boolean isBaselineMigration() {
      return false;
   }

   @Override
   public boolean canExecuteInTransaction() {
      return true;
   }
}
