package org.flywaydb.core.internal.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.output.ValidateResult;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;

public class DbValidate {
   private static final Log LOG = LogFactory.getLog(DbValidate.class);
   private final SchemaHistory schemaHistory;
   private final Schema schema;
   private final MigrationResolver migrationResolver;
   private final Connection connection;
   private final Configuration configuration;
   private final boolean pending;
   private final CallbackExecutor callbackExecutor;
   private final Database database;

   public DbValidate(
      Database database,
      SchemaHistory schemaHistory,
      Schema schema,
      MigrationResolver migrationResolver,
      Configuration configuration,
      boolean pending,
      CallbackExecutor callbackExecutor
   ) {
      this.database = database;
      this.connection = database.getMainConnection();
      this.schemaHistory = schemaHistory;
      this.schema = schema;
      this.migrationResolver = migrationResolver;
      this.configuration = configuration;
      this.pending = pending;
      this.callbackExecutor = callbackExecutor;
   }

   public ValidateResult validate() {
      if (!this.schema.exists()) {
         if (!this.migrationResolver.resolveMigrations(new Context() {
            @Override
            public Configuration getConfiguration() {
               return DbValidate.this.configuration;
            }
         }).isEmpty() && !this.pending) {
            String validationErrorMessage = "Schema " + this.schema + " doesn't exist yet";
            ErrorDetails validationError = new ErrorDetails(ErrorCode.SCHEMA_DOES_NOT_EXIST, validationErrorMessage);
            return CommandResultFactory.createValidateResult(this.database.getCatalog(), validationError, 0, null, new ArrayList());
         } else {
            return CommandResultFactory.createValidateResult(this.database.getCatalog(), null, 0, null, new ArrayList());
         }
      } else {
         this.callbackExecutor.onEvent(Event.BEFORE_VALIDATE);
         LOG.debug("Validating migrations ...");
         StopWatch stopWatch = new StopWatch();
         stopWatch.start();
         Pair<Integer, List<ValidateOutput>> result = ExecutionTemplateFactory.createExecutionTemplate(this.connection.getJdbcConnection(), this.database)
            .execute(
               new Callable<Pair<Integer, List<ValidateOutput>>>() {
                  public Pair<Integer, List<ValidateOutput>> call() {
                     MigrationInfoServiceImpl migrationInfoService = new MigrationInfoServiceImpl(
                        DbValidate.this.migrationResolver,
                        DbValidate.this.schemaHistory,
                        DbValidate.this.database,
                        DbValidate.this.configuration,
                        DbValidate.this.configuration.getTarget(),
                        DbValidate.this.configuration.isOutOfOrder(),
                        DbValidate.this.configuration.getCherryPick(),
                        DbValidate.this.pending,
                        DbValidate.this.configuration.isIgnoreMissingMigrations(),
                        DbValidate.this.configuration.isIgnoreIgnoredMigrations(),
                        DbValidate.this.configuration.isIgnoreFutureMigrations()
                     );
                     migrationInfoService.refresh();
                     int count = migrationInfoService.all().length;
                     List<ValidateOutput> invalidMigrations = migrationInfoService.validate();
                     return Pair.of(count, invalidMigrations);
                  }
               }
            );
         stopWatch.stop();
         List<String> warnings = new ArrayList();
         List<ValidateOutput> invalidMigrations = (List)result.getRight();
         ErrorDetails validationError = null;
         int count = 0;
         if (invalidMigrations.isEmpty()) {
            count = result.getLeft();
            if (count == 1) {
               LOG.info(String.format("Successfully validated 1 migration (execution time %s)", TimeFormat.format(stopWatch.getTotalTimeMillis())));
            } else {
               LOG.info(String.format("Successfully validated %d migrations (execution time %s)", count, TimeFormat.format(stopWatch.getTotalTimeMillis())));
               if (count == 0) {
                  String noMigrationsWarning = "No migrations found. Are your locations set up correctly?";
                  warnings.add(noMigrationsWarning);
                  LOG.warn(noMigrationsWarning);
               }
            }

            this.callbackExecutor.onEvent(Event.AFTER_VALIDATE);
         } else {
            validationError = new ErrorDetails(ErrorCode.VALIDATE_ERROR, "Migrations have failed validation");
            this.callbackExecutor.onEvent(Event.AFTER_VALIDATE_ERROR);
         }

         return CommandResultFactory.createValidateResult(this.database.getCatalog(), validationError, count, invalidMigrations, warnings);
      }
   }
}
