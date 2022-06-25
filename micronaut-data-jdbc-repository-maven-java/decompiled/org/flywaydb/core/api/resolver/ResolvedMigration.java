package org.flywaydb.core.api.resolver;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.executor.MigrationExecutor;

public interface ResolvedMigration extends ChecksumMatcher {
   MigrationVersion getVersion();

   String getDescription();

   String getScript();

   Integer getChecksum();

   MigrationType getType();

   String getPhysicalLocation();

   MigrationExecutor getExecutor();
}
