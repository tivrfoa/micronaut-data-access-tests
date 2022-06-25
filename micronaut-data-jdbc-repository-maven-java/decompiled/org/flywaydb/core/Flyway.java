package org.flywaydb.core;

import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.BaselineResult;
import org.flywaydb.core.api.output.CleanResult;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.output.RepairResult;
import org.flywaydb.core.api.output.UndoResult;
import org.flywaydb.core.api.output.ValidateResult;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.FlywayTeamsObjectResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.command.DbBaseline;
import org.flywaydb.core.internal.command.DbClean;
import org.flywaydb.core.internal.command.DbInfo;
import org.flywaydb.core.internal.command.DbMigrate;
import org.flywaydb.core.internal.command.DbRepair;
import org.flywaydb.core.internal.command.DbSchemas;
import org.flywaydb.core.internal.command.DbValidate;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.StringUtils;

public class Flyway {
   private static final Log LOG = LogFactory.getLog(Flyway.class);
   private final ClassicConfiguration configuration;
   private final FlywayExecutor flywayExecutor;

   public static FluentConfiguration configure() {
      return new FluentConfiguration();
   }

   public static FluentConfiguration configure(ClassLoader classLoader) {
      return new FluentConfiguration(classLoader);
   }

   public Flyway(Configuration configuration) {
      this.configuration = new ClassicConfiguration(configuration);
      this.configuration.loadCallbackLocation("db/callback", false);
      this.flywayExecutor = new FlywayExecutor(this.configuration);
      LogFactory.setConfiguration(this.configuration);
   }

   public Configuration getConfiguration() {
      return new ClassicConfiguration(this.configuration);
   }

   public MigrateResult migrate() throws FlywayException {
      return this.flywayExecutor
         .execute(
            new FlywayExecutor.Command<MigrateResult>() {
               public MigrateResult execute(
                  MigrationResolver migrationResolver,
                  SchemaHistory schemaHistory,
                  Database database,
                  Schema defaultSchema,
                  Schema[] schemas,
                  CallbackExecutor callbackExecutor,
                  StatementInterceptor statementInterceptor
               ) {
                  if (Flyway.this.configuration.isValidateOnMigrate()) {
                     ValidateResult validateResult = Flyway.this.doValidate(
                        database, migrationResolver, schemaHistory, defaultSchema, schemas, callbackExecutor, true
                     );
                     if (!validateResult.validationSuccessful && !Flyway.this.configuration.isCleanOnValidationError()) {
                        throw new FlywayValidateException(validateResult.errorDetails, validateResult.getAllErrorMessages());
                     }
                  }
      
                  if (!schemaHistory.exists()) {
                     List<Schema> nonEmptySchemas = new ArrayList();
      
                     for(Schema schema : schemas) {
                        if (schema.exists() && !schema.empty()) {
                           nonEmptySchemas.add(schema);
                        }
                     }
      
                     if (!nonEmptySchemas.isEmpty()) {
                        if (Flyway.this.configuration.isBaselineOnMigrate()) {
                           Flyway.this.doBaseline(schemaHistory, callbackExecutor, database);
                        } else if (!schemaHistory.exists()) {
                           throw new FlywayException(
                              "Found non-empty schema(s) "
                                 + StringUtils.collectionToCommaDelimitedString(nonEmptySchemas)
                                 + " but no schema history table. Use baseline() or set baselineOnMigrate to true to initialize the schema history table."
                           );
                        }
                     } else {
                        if (Flyway.this.configuration.isCreateSchemas()) {
                           new DbSchemas(database, schemas, schemaHistory, callbackExecutor).create(false);
                        } else if (!defaultSchema.exists()) {
                           Flyway.LOG
                              .warn(
                                 "The configuration option 'createSchemas' is false.\nHowever, the schema history table still needs a schema to reside in.\nYou must manually create a schema for the schema history table to reside in.\nSee https://flywaydb.org/documentation/concepts/migrations.html#the-createschemas-option-and-the-schema-history-table"
                              );
                        }
      
                        schemaHistory.create(false);
                     }
                  }
      
                  MigrateResult result = new DbMigrate(database, schemaHistory, defaultSchema, migrationResolver, Flyway.this.configuration, callbackExecutor)
                     .migrate();
                  callbackExecutor.onOperationFinishEvent(Event.AFTER_MIGRATE_OPERATION_FINISH, result);
                  return result;
               }
            },
            true
         );
   }

   public MigrationInfoService info() {
      return this.flywayExecutor
         .execute(
            new FlywayExecutor.Command<MigrationInfoService>() {
               public MigrationInfoService execute(
                  MigrationResolver migrationResolver,
                  SchemaHistory schemaHistory,
                  Database database,
                  Schema defaultSchema,
                  Schema[] schemas,
                  CallbackExecutor callbackExecutor,
                  StatementInterceptor statementInterceptor
               ) {
                  MigrationInfoService migrationInfoService = new DbInfo(
                        migrationResolver, schemaHistory, Flyway.this.configuration, database, callbackExecutor, schemas
                     )
                     .info();
                  callbackExecutor.onOperationFinishEvent(Event.AFTER_INFO_OPERATION_FINISH, migrationInfoService.getInfoResult());
                  return migrationInfoService;
               }
            },
            true
         );
   }

   public CleanResult clean() {
      return this.flywayExecutor
         .execute(
            new FlywayExecutor.Command<CleanResult>() {
               public CleanResult execute(
                  MigrationResolver migrationResolver,
                  SchemaHistory schemaHistory,
                  Database database,
                  Schema defaultSchema,
                  Schema[] schemas,
                  CallbackExecutor callbackExecutor,
                  StatementInterceptor statementInterceptor
               ) {
                  CleanResult cleanResult = Flyway.this.doClean(database, schemaHistory, defaultSchema, schemas, callbackExecutor);
                  callbackExecutor.onOperationFinishEvent(Event.AFTER_CLEAN_OPERATION_FINISH, cleanResult);
                  return cleanResult;
               }
            },
            false
         );
   }

   public void validate() throws FlywayException {
      this.flywayExecutor
         .execute(
            new FlywayExecutor.Command<Void>() {
               public Void execute(
                  MigrationResolver migrationResolver,
                  SchemaHistory schemaHistory,
                  Database database,
                  Schema defaultSchema,
                  Schema[] schemas,
                  CallbackExecutor callbackExecutor,
                  StatementInterceptor statementInterceptor
               ) {
                  ValidateResult validateResult = Flyway.this.doValidate(
                     database,
                     migrationResolver,
                     schemaHistory,
                     defaultSchema,
                     schemas,
                     callbackExecutor,
                     Flyway.this.configuration.isIgnorePendingMigrations()
                  );
                  callbackExecutor.onOperationFinishEvent(Event.AFTER_VALIDATE_OPERATION_FINISH, validateResult);
                  Flyway.LOG.notice("Automate migration testing for Database CI with Flyway Hub. Visit https://flywaydb.org/get-started-with-hub");
                  if (!validateResult.validationSuccessful && !Flyway.this.configuration.isCleanOnValidationError()) {
                     throw new FlywayValidateException(validateResult.errorDetails, validateResult.getAllErrorMessages());
                  } else {
                     return null;
                  }
               }
            },
            true
         );
   }

   public ValidateResult validateWithResult() throws FlywayException {
      return this.flywayExecutor
         .execute(
            new FlywayExecutor.Command<ValidateResult>() {
               public ValidateResult execute(
                  MigrationResolver migrationResolver,
                  SchemaHistory schemaHistory,
                  Database database,
                  Schema defaultSchema,
                  Schema[] schemas,
                  CallbackExecutor callbackExecutor,
                  StatementInterceptor statementInterceptor
               ) {
                  ValidateResult validateResult = Flyway.this.doValidate(
                     database,
                     migrationResolver,
                     schemaHistory,
                     defaultSchema,
                     schemas,
                     callbackExecutor,
                     Flyway.this.configuration.isIgnorePendingMigrations()
                  );
                  callbackExecutor.onOperationFinishEvent(Event.AFTER_VALIDATE_OPERATION_FINISH, validateResult);
                  return validateResult;
               }
            },
            true
         );
   }

   public BaselineResult baseline() throws FlywayException {
      return this.flywayExecutor
         .execute(
            new FlywayExecutor.Command<BaselineResult>() {
               public BaselineResult execute(
                  MigrationResolver migrationResolver,
                  SchemaHistory schemaHistory,
                  Database database,
                  Schema defaultSchema,
                  Schema[] schemas,
                  CallbackExecutor callbackExecutor,
                  StatementInterceptor statementInterceptor
               ) {
                  if (Flyway.this.configuration.isCreateSchemas()) {
                     new DbSchemas(database, schemas, schemaHistory, callbackExecutor).create(true);
                  } else {
                     Flyway.LOG
                        .warn(
                           "The configuration option 'createSchemas' is false.\nEven though Flyway is configured not to create any schemas, the schema history table still needs a schema to reside in.\nYou must manually create a schema for the schema history table to reside in.\nSee https://flywaydb.org/documentation/concepts/migrations.html#the-createschemas-option-and-the-schema-history-table"
                        );
                  }
      
                  BaselineResult baselineResult = Flyway.this.doBaseline(schemaHistory, callbackExecutor, database);
                  callbackExecutor.onOperationFinishEvent(Event.AFTER_BASELINE_OPERATION_FINISH, baselineResult);
                  return baselineResult;
               }
            },
            false
         );
   }

   public RepairResult repair() throws FlywayException {
      return this.flywayExecutor
         .execute(
            new FlywayExecutor.Command<RepairResult>() {
               public RepairResult execute(
                  MigrationResolver migrationResolver,
                  SchemaHistory schemaHistory,
                  Database database,
                  Schema defaultSchema,
                  Schema[] schemas,
                  CallbackExecutor callbackExecutor,
                  StatementInterceptor statementInterceptor
               ) {
                  RepairResult repairResult = new DbRepair(database, migrationResolver, schemaHistory, callbackExecutor, Flyway.this.configuration).repair();
                  callbackExecutor.onOperationFinishEvent(Event.AFTER_REPAIR_OPERATION_FINISH, repairResult);
                  return repairResult;
               }
            },
            true
         );
   }

   public UndoResult undo() throws FlywayException {
      throw new FlywayTeamsUpgradeRequiredException("undo");
   }

   private CleanResult doClean(Database database, SchemaHistory schemaHistory, Schema defaultSchema, Schema[] schemas, CallbackExecutor callbackExecutor) {
      return FlywayTeamsObjectResolver.<DbClean>resolve(DbClean.class, database, schemaHistory, defaultSchema, schemas, callbackExecutor, this.configuration)
         .clean();
   }

   private ValidateResult doValidate(
      Database database,
      MigrationResolver migrationResolver,
      SchemaHistory schemaHistory,
      Schema defaultSchema,
      Schema[] schemas,
      CallbackExecutor callbackExecutor,
      boolean ignorePending
   ) {
      ValidateResult validateResult = new DbValidate(
            database, schemaHistory, defaultSchema, migrationResolver, this.configuration, ignorePending, callbackExecutor
         )
         .validate();
      if (!validateResult.validationSuccessful && this.configuration.isCleanOnValidationError()) {
         this.doClean(database, schemaHistory, defaultSchema, schemas, callbackExecutor);
      }

      return validateResult;
   }

   private BaselineResult doBaseline(SchemaHistory schemaHistory, CallbackExecutor callbackExecutor, Database database) {
      return new DbBaseline(schemaHistory, this.configuration.getBaselineVersion(), this.configuration.getBaselineDescription(), callbackExecutor, database)
         .baseline();
   }
}
