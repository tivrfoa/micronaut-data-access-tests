package org.flywaydb.core.internal.resolver.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.util.ClassUtils;

public class ScanningJavaMigrationResolver implements MigrationResolver {
   private final ClassProvider<JavaMigration> classProvider;
   private final Configuration configuration;

   public List<ResolvedMigration> resolveMigrations(Context context) {
      List<ResolvedMigration> migrations = new ArrayList();

      for(Class<?> clazz : this.classProvider.getClasses()) {
         JavaMigration javaMigration = ClassUtils.instantiate(clazz.getName(), this.configuration.getClassLoader());
         migrations.add(new ResolvedJavaMigration(javaMigration));
      }

      Collections.sort(migrations, new ResolvedMigrationComparator());
      return migrations;
   }

   public ScanningJavaMigrationResolver(ClassProvider<JavaMigration> classProvider, Configuration configuration) {
      this.classProvider = classProvider;
      this.configuration = configuration;
   }
}
