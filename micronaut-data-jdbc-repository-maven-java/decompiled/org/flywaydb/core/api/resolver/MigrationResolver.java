package org.flywaydb.core.api.resolver;

import java.util.Collection;

public interface MigrationResolver {
   Collection<ResolvedMigration> resolveMigrations(Context var1);
}
