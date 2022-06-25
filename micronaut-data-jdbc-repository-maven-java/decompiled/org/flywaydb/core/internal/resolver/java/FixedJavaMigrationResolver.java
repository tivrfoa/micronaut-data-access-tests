package org.flywaydb.core.internal.resolver.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;

public class FixedJavaMigrationResolver implements MigrationResolver {
   private final JavaMigration[] javaMigrations;

   public FixedJavaMigrationResolver(JavaMigration... javaMigrations) {
      this.javaMigrations = javaMigrations;
   }

   public List<ResolvedMigration> resolveMigrations(Context context) {
      List<ResolvedMigration> migrations = new ArrayList();

      for(JavaMigration javaMigration : this.javaMigrations) {
         migrations.add(new ResolvedJavaMigration(javaMigration));
      }

      Collections.sort(migrations, new ResolvedMigrationComparator());
      return migrations;
   }
}
