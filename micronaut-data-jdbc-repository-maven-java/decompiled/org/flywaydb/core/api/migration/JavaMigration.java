package org.flywaydb.core.api.migration;

import org.flywaydb.core.api.MigrationVersion;

public interface JavaMigration {
   MigrationVersion getVersion();

   String getDescription();

   Integer getChecksum();

   boolean isUndo();

   boolean isBaselineMigration();

   boolean canExecuteInTransaction();

   void migrate(Context var1) throws Exception;
}
