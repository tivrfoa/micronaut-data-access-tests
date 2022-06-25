package org.flywaydb.core.internal.resolver.sql;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

public class SqlMigrationResolver implements MigrationResolver {
   private static final Log LOG = LogFactory.getLog(SqlMigrationResolver.class);
   private final SqlScriptExecutorFactory sqlScriptExecutorFactory;
   private final ResourceProvider resourceProvider;
   private final SqlScriptFactory sqlScriptFactory;
   private final Configuration configuration;
   private final ParsingContext parsingContext;

   public SqlMigrationResolver(
      ResourceProvider resourceProvider,
      SqlScriptExecutorFactory sqlScriptExecutorFactory,
      SqlScriptFactory sqlScriptFactory,
      Configuration configuration,
      ParsingContext parsingContext
   ) {
      this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
      this.resourceProvider = resourceProvider;
      this.sqlScriptFactory = sqlScriptFactory;
      this.configuration = configuration;
      this.parsingContext = parsingContext;
   }

   public List<ResolvedMigration> resolveMigrations(Context context) {
      List<ResolvedMigration> migrations = new ArrayList();
      String[] suffixes = this.configuration.getSqlMigrationSuffixes();
      this.addMigrations(migrations, this.configuration.getSqlMigrationPrefix(), suffixes, false);
      this.addMigrations(migrations, this.configuration.getRepeatableSqlMigrationPrefix(), suffixes, true);
      migrations.sort(new ResolvedMigrationComparator());
      return migrations;
   }

   private LoadableResource[] createPlaceholderReplacingLoadableResources(List<LoadableResource> loadableResources) {
      List<LoadableResource> list = new ArrayList();

      for(final LoadableResource loadableResource : loadableResources) {
         LoadableResource placeholderReplacingLoadableResource = new LoadableResource() {
            @Override
            public Reader read() {
               return PlaceholderReplacingReader.create(
                  SqlMigrationResolver.this.configuration, SqlMigrationResolver.this.parsingContext, loadableResource.read()
               );
            }

            @Override
            public String getAbsolutePath() {
               return loadableResource.getAbsolutePath();
            }

            @Override
            public String getAbsolutePathOnDisk() {
               return loadableResource.getAbsolutePathOnDisk();
            }

            @Override
            public String getFilename() {
               return loadableResource.getFilename();
            }

            @Override
            public String getRelativePath() {
               return loadableResource.getRelativePath();
            }
         };
         list.add(placeholderReplacingLoadableResource);
      }

      return (LoadableResource[])list.toArray(new LoadableResource[0]);
   }

   private Integer getChecksumForLoadableResource(boolean repeatable, List<LoadableResource> loadableResources) {
      return repeatable && this.configuration.isPlaceholderReplacement()
         ? ChecksumCalculator.calculate(this.createPlaceholderReplacingLoadableResources(loadableResources))
         : ChecksumCalculator.calculate((LoadableResource[])loadableResources.toArray(new LoadableResource[0]));
   }

   private Integer getEquivalentChecksumForLoadableResource(boolean repeatable, List<LoadableResource> loadableResources) {
      return repeatable ? ChecksumCalculator.calculate((LoadableResource[])loadableResources.toArray(new LoadableResource[0])) : null;
   }

   private void addMigrations(List<ResolvedMigration> migrations, String prefix, String[] suffixes, boolean repeatable) {
      ResourceNameParser resourceNameParser = new ResourceNameParser(this.configuration);

      for(LoadableResource resource : this.resourceProvider.getResources(prefix, suffixes)) {
         String filename = resource.getFilename();
         ResourceName result = resourceNameParser.parse(filename);
         if (result.isValid() && !isSqlCallback(result) && prefix.equals(result.getPrefix())) {
            SqlScript sqlScript = this.sqlScriptFactory.createSqlScript(resource, this.configuration.isMixed(), this.resourceProvider);
            List<LoadableResource> resources = new ArrayList();
            resources.add(resource);
            Integer checksum = this.getChecksumForLoadableResource(repeatable, resources);
            Integer equivalentChecksum = this.getEquivalentChecksumForLoadableResource(repeatable, resources);
            migrations.add(
               new ResolvedMigrationImpl(
                  result.getVersion(),
                  result.getDescription(),
                  resource.getRelativePath(),
                  checksum,
                  equivalentChecksum,
                  MigrationType.SQL,
                  resource.getAbsolutePathOnDisk(),
                  new SqlMigrationExecutor(this.sqlScriptExecutorFactory, sqlScript, false, false)
               ) {
                  @Override
                  public void validate() {
                  }
               }
            );
         }
      }

   }

   protected static boolean isSqlCallback(ResourceName result) {
      return Event.fromId(result.getPrefix()) != null;
   }
}
