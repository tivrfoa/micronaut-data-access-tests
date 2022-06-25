package org.flywaydb.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.callback.DefaultCallbackExecutor;
import org.flywaydb.core.internal.callback.NoopCallbackExecutor;
import org.flywaydb.core.internal.callback.SqlScriptCallbackFactory;
import org.flywaydb.core.internal.clazz.NoopClassProvider;
import org.flywaydb.core.internal.configuration.ConfigurationValidator;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.resource.NoopResourceProvider;
import org.flywaydb.core.internal.resource.ResourceNameValidator;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.Scanner;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.schemahistory.SchemaHistoryFactory;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.strategy.RetryStrategy;
import org.flywaydb.core.internal.util.DataUnits;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.Pair;

public class FlywayExecutor {
   private static final Log LOG = LogFactory.getLog(FlywayExecutor.class);
   private final ConfigurationValidator configurationValidator = new ConfigurationValidator();
   private final ResourceNameValidator resourceNameValidator = new ResourceNameValidator();
   private final ResourceNameCache resourceNameCache = new ResourceNameCache();
   private final LocationScannerCache locationScannerCache = new LocationScannerCache();
   private boolean dbConnectionInfoPrinted;
   private final Configuration configuration;

   public FlywayExecutor(Configuration configuration) {
      this.configuration = configuration;
   }

   public <T> T execute(FlywayExecutor.Command<T> command, boolean scannerRequired) {
      this.configurationValidator.validate(this.configuration);
      VersionPrinter.printVersion();
      StatementInterceptor statementInterceptor = null;
      Pair<ResourceProvider, ClassProvider<JavaMigration>> resourceProviderClassProviderPair = this.createResourceAndClassProviders(scannerRequired);
      ResourceProvider resourceProvider = resourceProviderClassProviderPair.getLeft();
      ClassProvider<JavaMigration> classProvider = resourceProviderClassProviderPair.getRight();
      ParsingContext parsingContext = new ParsingContext();
      this.resourceNameValidator.validateSQLMigrationNaming(resourceProvider, this.configuration);
      JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory(this.configuration.getDataSource(), this.configuration, statementInterceptor);
      DatabaseType databaseType = jdbcConnectionFactory.getDatabaseType();
      SqlScriptFactory sqlScriptFactory = databaseType.createSqlScriptFactory(this.configuration, parsingContext);
      RetryStrategy.setNumberOfRetries(this.configuration.getLockRetryCount());
      SqlScriptExecutorFactory noCallbackSqlScriptExecutorFactory = databaseType.createSqlScriptExecutorFactory(
         jdbcConnectionFactory, NoopCallbackExecutor.INSTANCE, null
      );
      jdbcConnectionFactory.setConnectionInitializer((jdbcConnectionFactory1, connection) -> {
         if (this.configuration.getInitSql() != null) {
            StringResource resource = new StringResource(this.configuration.getInitSql());
            SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource, true, resourceProvider);
            boolean outputQueryResults = false;
            noCallbackSqlScriptExecutorFactory.createSqlScriptExecutor(connection, false, false, outputQueryResults).execute(sqlScript);
         }
      });
      Database database = null;

      T result;
      try {
         database = databaseType.createDatabase(this.configuration, !this.dbConnectionInfoPrinted, jdbcConnectionFactory, statementInterceptor);
         databaseType.printMessages();
         this.dbConnectionInfoPrinted = true;
         LOG.debug("DDL Transactions Supported: " + database.supportsDdlTransactions());
         Pair<Schema, List<Schema>> schemas = SchemaHistoryFactory.prepareSchemas(this.configuration, database);
         Schema defaultSchema = schemas.getLeft();
         parsingContext.populate(database, this.configuration);
         database.ensureSupported();
         DefaultCallbackExecutor callbackExecutor = new DefaultCallbackExecutor(
            this.configuration,
            database,
            defaultSchema,
            this.prepareCallbacks(database, resourceProvider, jdbcConnectionFactory, sqlScriptFactory, statementInterceptor, defaultSchema, parsingContext)
         );
         SqlScriptExecutorFactory sqlScriptExecutorFactory = databaseType.createSqlScriptExecutorFactory(
            jdbcConnectionFactory, callbackExecutor, statementInterceptor
         );
         SchemaHistory schemaHistory = SchemaHistoryFactory.getSchemaHistory(
            this.configuration, noCallbackSqlScriptExecutorFactory, sqlScriptFactory, database, defaultSchema, statementInterceptor
         );
         result = command.execute(
            this.createMigrationResolver(resourceProvider, classProvider, sqlScriptExecutorFactory, sqlScriptFactory, parsingContext),
            schemaHistory,
            database,
            defaultSchema,
            (Schema[])((List)schemas.getRight()).toArray(new Schema[0]),
            callbackExecutor,
            statementInterceptor
         );
      } finally {
         IOUtils.close(database);
         this.showMemoryUsage();
      }

      return result;
   }

   private Pair<ResourceProvider, ClassProvider<JavaMigration>> createResourceAndClassProviders(boolean scannerRequired) {
      ResourceProvider resourceProvider;
      ClassProvider<JavaMigration> classProvider;
      if (!scannerRequired && this.configuration.isSkipDefaultResolvers() && this.configuration.isSkipDefaultCallbacks()) {
         resourceProvider = NoopResourceProvider.INSTANCE;
         classProvider = NoopClassProvider.INSTANCE;
      } else if (this.configuration.getResourceProvider() != null && this.configuration.getJavaMigrationClassProvider() != null) {
         resourceProvider = this.configuration.getResourceProvider();
         classProvider = this.configuration.getJavaMigrationClassProvider();
      } else {
         boolean stream = false;
         Scanner<JavaMigration> scanner = new Scanner<>(
            JavaMigration.class,
            Arrays.asList(this.configuration.getLocations()),
            this.configuration.getClassLoader(),
            this.configuration.getEncoding(),
            this.configuration.isDetectEncoding(),
            stream,
            this.resourceNameCache,
            this.locationScannerCache,
            this.configuration.isFailOnMissingLocations()
         );
         resourceProvider = scanner;
         classProvider = scanner;
         if (this.configuration.getResourceProvider() != null) {
            resourceProvider = this.configuration.getResourceProvider();
         }

         if (this.configuration.getJavaMigrationClassProvider() != null) {
            classProvider = this.configuration.getJavaMigrationClassProvider();
         }
      }

      return Pair.of(resourceProvider, classProvider);
   }

   private List<Callback> prepareCallbacks(
      Database database,
      ResourceProvider resourceProvider,
      JdbcConnectionFactory jdbcConnectionFactory,
      SqlScriptFactory sqlScriptFactory,
      StatementInterceptor statementInterceptor,
      Schema schema,
      ParsingContext parsingContext
   ) {
      List<Callback> effectiveCallbacks = new ArrayList();
      CallbackExecutor callbackExecutor = NoopCallbackExecutor.INSTANCE;
      effectiveCallbacks.addAll(Arrays.asList(this.configuration.getCallbacks()));
      if (!this.configuration.isSkipDefaultCallbacks()) {
         SqlScriptExecutorFactory sqlScriptExecutorFactory = jdbcConnectionFactory.getDatabaseType()
            .createSqlScriptExecutorFactory(jdbcConnectionFactory, callbackExecutor, statementInterceptor);
         effectiveCallbacks.addAll(
            new SqlScriptCallbackFactory(resourceProvider, sqlScriptExecutorFactory, sqlScriptFactory, this.configuration).getCallbacks()
         );
      }

      return effectiveCallbacks;
   }

   private MigrationResolver createMigrationResolver(
      ResourceProvider resourceProvider,
      ClassProvider<JavaMigration> classProvider,
      SqlScriptExecutorFactory sqlScriptExecutorFactory,
      SqlScriptFactory sqlScriptFactory,
      ParsingContext parsingContext
   ) {
      return new CompositeMigrationResolver(
         resourceProvider, classProvider, this.configuration, sqlScriptExecutorFactory, sqlScriptFactory, parsingContext, this.configuration.getResolvers()
      );
   }

   private void showMemoryUsage() {
      Runtime runtime = Runtime.getRuntime();
      long free = runtime.freeMemory();
      long total = runtime.totalMemory();
      long used = total - free;
      long totalMB = DataUnits.MEGABYTE.fromBytes(total);
      long usedMB = DataUnits.MEGABYTE.fromBytes(used);
      LOG.debug("Memory usage: " + usedMB + " of " + totalMB + "M");
   }

   public interface Command<T> {
      T execute(MigrationResolver var1, SchemaHistory var2, Database var3, Schema var4, Schema[] var5, CallbackExecutor var6, StatementInterceptor var7);
   }
}
