package org.flywaydb.core.internal.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.java.FixedJavaMigrationResolver;
import org.flywaydb.core.internal.resolver.java.ScanningJavaMigrationResolver;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

public class CompositeMigrationResolver implements MigrationResolver {
   private final Collection<MigrationResolver> migrationResolvers = new ArrayList();
   private List<ResolvedMigration> availableMigrations;

   public CompositeMigrationResolver(
      ResourceProvider resourceProvider,
      ClassProvider<JavaMigration> classProvider,
      Configuration configuration,
      SqlScriptExecutorFactory sqlScriptExecutorFactory,
      SqlScriptFactory sqlScriptFactory,
      ParsingContext parsingContext,
      MigrationResolver... customMigrationResolvers
   ) {
      if (!configuration.isSkipDefaultResolvers()) {
         this.migrationResolvers.add(new SqlMigrationResolver(resourceProvider, sqlScriptExecutorFactory, sqlScriptFactory, configuration, parsingContext));
         this.migrationResolvers.add(new ScanningJavaMigrationResolver(classProvider, configuration));
      }

      this.migrationResolvers.add(new FixedJavaMigrationResolver(configuration.getJavaMigrations()));
      this.migrationResolvers.addAll(Arrays.asList(customMigrationResolvers));
   }

   public List<ResolvedMigration> resolveMigrations(Context context) {
      if (this.availableMigrations == null) {
         this.availableMigrations = this.doFindAvailableMigrations(context);
      }

      return this.availableMigrations;
   }

   private List<ResolvedMigration> doFindAvailableMigrations(Context context) throws FlywayException {
      List<ResolvedMigration> migrations = new ArrayList(collectMigrations(this.migrationResolvers, context));
      migrations.sort(new ResolvedMigrationComparator());
      checkForIncompatibilities(migrations);
      return migrations;
   }

   static Collection<ResolvedMigration> collectMigrations(Collection<MigrationResolver> migrationResolvers, Context context) {
      Set<ResolvedMigration> migrations = new HashSet();

      for(MigrationResolver migrationResolver : migrationResolvers) {
         migrations.addAll(migrationResolver.resolveMigrations(context));
      }

      return migrations;
   }

   static void checkForIncompatibilities(List<ResolvedMigration> migrations) {
      ResolvedMigrationComparator resolvedMigrationComparator = new ResolvedMigrationComparator();

      for(int i = 0; i < migrations.size() - 1; ++i) {
         ResolvedMigration current = (ResolvedMigration)migrations.get(i);
         ResolvedMigration next = (ResolvedMigration)migrations.get(i + 1);
         if (resolvedMigrationComparator.compare(current, next) == 0) {
            if (current.getVersion() != null) {
               throw new FlywayException(
                  String.format(
                     "Found more than one migration with version %s\nOffenders:\n-> %s (%s)\n-> %s (%s)",
                     current.getVersion(),
                     current.getPhysicalLocation(),
                     current.getType(),
                     next.getPhysicalLocation(),
                     next.getType()
                  ),
                  ErrorCode.DUPLICATE_VERSIONED_MIGRATION
               );
            }

            throw new FlywayException(
               String.format(
                  "Found more than one repeatable migration with description %s\nOffenders:\n-> %s (%s)\n-> %s (%s)",
                  current.getDescription(),
                  current.getPhysicalLocation(),
                  current.getType(),
                  next.getPhysicalLocation(),
                  next.getType()
               ),
               ErrorCode.DUPLICATE_REPEATABLE_MIGRATION
            );
         }
      }

   }
}
