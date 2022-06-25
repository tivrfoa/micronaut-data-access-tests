package org.flywaydb.core.internal.command;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.MigrateErrorResult;
import org.flywaydb.core.api.output.MigrateOutput;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.TimeFormat;

public class DbMigrate {
   private static final Log LOG = LogFactory.getLog(DbMigrate.class);
   private final Database database;
   private final SchemaHistory schemaHistory;
   private final Schema schema;
   private final MigrationResolver migrationResolver;
   private final Configuration configuration;
   private final CallbackExecutor callbackExecutor;
   private final Connection connectionUserObjects;
   private MigrateResult migrateResult;
   private boolean isPreviousVersioned;
   private final List<ResolvedMigration> appliedResolvedMigrations = new ArrayList();

   public DbMigrate(
      Database database,
      SchemaHistory schemaHistory,
      Schema schema,
      MigrationResolver migrationResolver,
      Configuration configuration,
      CallbackExecutor callbackExecutor
   ) {
      this.database = database;
      this.connectionUserObjects = database.getMigrationConnection();
      this.schemaHistory = schemaHistory;
      this.schema = schema;
      this.migrationResolver = migrationResolver;
      this.configuration = configuration;
      this.callbackExecutor = callbackExecutor;
   }

   public MigrateResult migrate() throws FlywayException {
      this.callbackExecutor.onMigrateOrUndoEvent(Event.BEFORE_MIGRATE);
      this.migrateResult = CommandResultFactory.createMigrateResult(this.database.getCatalog(), this.configuration);

      int count;
      try {
         StopWatch stopWatch = new StopWatch();
         stopWatch.start();
         count = this.configuration.isGroup() ? this.schemaHistory.lock(this::migrateAll) : this.migrateAll();
         stopWatch.stop();
         this.migrateResult.targetSchemaVersion = this.getTargetVersion();
         this.migrateResult.migrationsExecuted = count;
         this.logSummary(count, stopWatch.getTotalTimeMillis(), this.migrateResult.targetSchemaVersion);
      } catch (FlywayException var3) {
         this.callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_MIGRATE_ERROR);
         throw var3;
      }

      if (count > 0) {
         this.callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_MIGRATE_APPLIED);
      }

      this.callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_MIGRATE);
      return this.migrateResult;
   }

   private String getTargetVersion() {
      if (!this.migrateResult.migrations.isEmpty()) {
         for(int i = this.migrateResult.migrations.size() - 1; i >= 0; --i) {
            String targetVersion = ((MigrateOutput)this.migrateResult.migrations.get(i)).version;
            if (!targetVersion.isEmpty()) {
               return targetVersion;
            }
         }
      }

      return null;
   }

   private int migrateAll() {
      int total = 0;
      this.isPreviousVersioned = true;

      int count;
      do {
         boolean firstRun = total == 0;
         count = this.configuration.isGroup() ? this.migrateGroup(firstRun) : this.schemaHistory.lock(() -> this.migrateGroup(firstRun));
         this.migrateResult.migrationsExecuted += count;
         total += count;
      } while(count != 0 && this.configuration.getTarget() != MigrationVersion.NEXT);

      if (this.isPreviousVersioned) {
         this.callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_VERSIONED);
      }

      return total;
   }

   private Integer migrateGroup(boolean firstRun) {
      MigrationInfoServiceImpl infoService = new MigrationInfoServiceImpl(
         this.migrationResolver,
         this.schemaHistory,
         this.database,
         this.configuration,
         this.configuration.getTarget(),
         this.configuration.isOutOfOrder(),
         this.configuration.getCherryPick(),
         true,
         true,
         true,
         true
      );
      infoService.refresh();
      MigrationInfo current = infoService.current();
      MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
      if (firstRun) {
         LOG.info("Current version of schema " + this.schema + ": " + currentSchemaVersion);
         MigrationVersion schemaVersionToOutput = currentSchemaVersion == null ? MigrationVersion.EMPTY : currentSchemaVersion;
         this.migrateResult.initialSchemaVersion = schemaVersionToOutput.getVersion();
         if (this.configuration.isOutOfOrder()) {
            String outOfOrderWarning = "outOfOrder mode is active. Migration of schema " + this.schema + " may not be reproducible.";
            LOG.warn(outOfOrderWarning);
            this.migrateResult.warnings.add(outOfOrderWarning);
         }
      }

      MigrationInfo[] future = infoService.future();
      if (future.length > 0) {
         List<MigrationInfo> resolved = Arrays.asList(infoService.resolved());
         Collections.reverse(resolved);
         if (resolved.isEmpty()) {
            LOG.error("Schema " + this.schema + " has version " + currentSchemaVersion + ", but no migration could be resolved in the configured locations !");
         } else {
            for(MigrationInfo migrationInfo : resolved) {
               if (migrationInfo.getVersion() != null) {
                  LOG.warn(
                     "Schema "
                        + this.schema
                        + " has a version ("
                        + currentSchemaVersion
                        + ") that is newer than the latest available migration ("
                        + migrationInfo.getVersion()
                        + ") !"
                  );
                  break;
               }
            }
         }
      }

      MigrationInfoImpl[] failed = infoService.failed();
      if (failed.length > 0) {
         if (failed.length != 1 || failed[0].getState() != MigrationState.FUTURE_FAILED || !this.configuration.isIgnoreFutureMigrations()) {
            boolean inTransaction = failed[0].canExecuteInTransaction();
            if (failed[0].getVersion() == null) {
               throw new DbMigrate.FlywayMigrateException(
                  failed[0],
                  "Schema " + this.schema + " contains a failed repeatable migration (" + this.doQuote(failed[0].getDescription()) + ") !",
                  inTransaction,
                  this.migrateResult
               );
            } else {
               throw new DbMigrate.FlywayMigrateException(
                  failed[0],
                  "Schema " + this.schema + " contains a failed migration to version " + failed[0].getVersion() + " !",
                  inTransaction,
                  this.migrateResult
               );
            }
         }

         LOG.warn("Schema " + this.schema + " contains a failed future migration to version " + failed[0].getVersion() + " !");
      }

      LinkedHashMap<MigrationInfoImpl, Boolean> group = new LinkedHashMap();

      for(MigrationInfoImpl pendingMigration : infoService.pending()) {
         if (!this.appliedResolvedMigrations.contains(pendingMigration.getResolvedMigration())) {
            boolean isOutOfOrder = pendingMigration.getVersion() != null && pendingMigration.getVersion().compareTo(currentSchemaVersion) < 0;
            group.put(pendingMigration, isOutOfOrder);
            if (!this.configuration.isGroup()) {
               break;
            }
         }
      }

      if (!group.isEmpty()) {
         boolean skipExecutingMigrations = false;
         this.applyMigrations(group, skipExecutingMigrations);
      }

      return group.size();
   }

   private void logSummary(int migrationSuccessCount, long executionTime, String targetVersion) {
      if (migrationSuccessCount == 0) {
         LOG.info("Schema " + this.schema + " is up to date. No migration necessary.");
      } else {
         String targetText = targetVersion != null ? ", now at version v" + targetVersion : "";
         String migrationText = migrationSuccessCount == 1 ? "migration" : "migrations";
         LOG.info(
            "Successfully applied "
               + migrationSuccessCount
               + " "
               + migrationText
               + " to schema "
               + this.schema
               + targetText
               + " (execution time "
               + TimeFormat.format(executionTime)
               + ")"
         );
      }
   }

   private void applyMigrations(LinkedHashMap<MigrationInfoImpl, Boolean> group, boolean skipExecutingMigrations) {
      boolean executeGroupInTransaction = this.isExecuteGroupInTransaction(group);
      StopWatch stopWatch = new StopWatch();

      try {
         if (executeGroupInTransaction) {
            ExecutionTemplateFactory.createExecutionTemplate(this.connectionUserObjects.getJdbcConnection(), this.database).execute(() -> {
               this.doMigrateGroup(group, stopWatch, skipExecutingMigrations, true);
               return null;
            });
         } else {
            this.doMigrateGroup(group, stopWatch, skipExecutingMigrations, false);
         }

      } catch (DbMigrate.FlywayMigrateException var9) {
         MigrationInfo migration = var9.getMigration();
         String failedMsg = "Migration of " + this.toMigrationText(migration, var9.isExecutableInTransaction(), var9.isOutOfOrder()) + " failed!";
         if (this.database.supportsDdlTransactions() && executeGroupInTransaction) {
            LOG.error(failedMsg + " Changes successfully rolled back.");
         } else {
            LOG.error(failedMsg + " Please restore backups and roll back database and code!");
            stopWatch.stop();
            int executionTime = (int)stopWatch.getTotalTimeMillis();
            this.schemaHistory
               .addAppliedMigration(
                  migration.getVersion(), migration.getDescription(), migration.getType(), migration.getScript(), migration.getChecksum(), executionTime, false
               );
         }

         throw var9;
      }
   }

   private boolean isExecuteGroupInTransaction(LinkedHashMap<MigrationInfoImpl, Boolean> group) {
      boolean executeGroupInTransaction = true;
      boolean first = true;

      for(Entry<MigrationInfoImpl, Boolean> entry : group.entrySet()) {
         ResolvedMigration resolvedMigration = ((MigrationInfoImpl)entry.getKey()).getResolvedMigration();
         boolean inTransaction = resolvedMigration.getExecutor().canExecuteInTransaction();
         if (first) {
            executeGroupInTransaction = inTransaction;
            first = false;
         } else {
            if (!this.configuration.isMixed() && executeGroupInTransaction != inTransaction) {
               throw new DbMigrate.FlywayMigrateException(
                  (MigrationInfo)entry.getKey(),
                  "Detected both transactional and non-transactional migrations within the same migration group (even though mixed is false). First offending migration: "
                     + this.doQuote(
                        (resolvedMigration.getVersion() == null ? "" : resolvedMigration.getVersion())
                           + (StringUtils.hasLength(resolvedMigration.getDescription()) ? " " + resolvedMigration.getDescription() : "")
                     )
                     + (inTransaction ? "" : " [non-transactional]"),
                  inTransaction,
                  this.migrateResult
               );
            }

            executeGroupInTransaction &= inTransaction;
         }
      }

      return executeGroupInTransaction;
   }

   private void doMigrateGroup(
      LinkedHashMap<MigrationInfoImpl, Boolean> group, StopWatch stopWatch, boolean skipExecutingMigrations, boolean isExecuteInTransaction
   ) {
      Context context = new Context() {
         @Override
         public Configuration getConfiguration() {
            return DbMigrate.this.configuration;
         }

         @Override
         public java.sql.Connection getConnection() {
            return DbMigrate.this.connectionUserObjects.getJdbcConnection();
         }
      };

      for(Entry<MigrationInfoImpl, Boolean> entry : group.entrySet()) {
         MigrationInfoImpl migration = (MigrationInfoImpl)entry.getKey();
         boolean isOutOfOrder = entry.getValue();
         String migrationText = this.toMigrationText(migration, migration.canExecuteInTransaction(), isOutOfOrder);
         stopWatch.start();
         if (this.isPreviousVersioned && migration.getVersion() == null) {
            this.callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_VERSIONED);
            this.callbackExecutor.onMigrateOrUndoEvent(Event.BEFORE_REPEATABLES);
            this.isPreviousVersioned = false;
         }

         if (skipExecutingMigrations) {
            LOG.debug("Skipping execution of migration of " + migrationText);
         } else {
            LOG.debug("Starting migration of " + migrationText + " ...");
            this.connectionUserObjects.restoreOriginalState();
            this.connectionUserObjects.changeCurrentSchemaTo(this.schema);

            try {
               this.callbackExecutor.setMigrationInfo(migration);
               this.callbackExecutor.onEachMigrateOrUndoEvent(Event.BEFORE_EACH_MIGRATE);

               try {
                  LOG.info("Migrating " + migrationText);
                  boolean oldAutoCommit = context.getConnection().getAutoCommit();
                  if (this.database.useSingleConnection() && !isExecuteInTransaction) {
                     context.getConnection().setAutoCommit(true);
                  }

                  migration.getResolvedMigration().getExecutor().execute(context);
                  if (this.database.useSingleConnection() && !isExecuteInTransaction) {
                     context.getConnection().setAutoCommit(oldAutoCommit);
                  }

                  this.appliedResolvedMigrations.add(migration.getResolvedMigration());
               } catch (FlywayException var16) {
                  this.callbackExecutor.onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE_ERROR);
                  throw new DbMigrate.FlywayMigrateException(migration, isOutOfOrder, var16, migration.canExecuteInTransaction(), this.migrateResult);
               } catch (SQLException var17) {
                  this.callbackExecutor.onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE_ERROR);
                  throw new DbMigrate.FlywayMigrateException(migration, isOutOfOrder, var17, migration.canExecuteInTransaction(), this.migrateResult);
               }

               LOG.debug("Successfully completed migration of " + migrationText);
               this.callbackExecutor.onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE);
            } finally {
               this.callbackExecutor.setMigrationInfo(null);
            }
         }

         stopWatch.stop();
         int executionTime = (int)stopWatch.getTotalTimeMillis();
         this.migrateResult.migrations.add(CommandResultFactory.createMigrateOutput(migration, executionTime));
         this.schemaHistory
            .addAppliedMigration(
               migration.getVersion(),
               migration.getDescription(),
               migration.getType(),
               migration.getScript(),
               migration.getResolvedMigration().getChecksum(),
               executionTime,
               true
            );
      }

   }

   private String toMigrationText(MigrationInfo migration, boolean canExecuteInTransaction, boolean isOutOfOrder) {
      String migrationText;
      if (migration.getVersion() != null) {
         migrationText = "schema "
            + this.schema
            + " to version "
            + this.doQuote(migration.getVersion() + (StringUtils.hasLength(migration.getDescription()) ? " - " + migration.getDescription() : ""))
            + (isOutOfOrder ? " [out of order]" : "")
            + (canExecuteInTransaction ? "" : " [non-transactional]");
      } else {
         migrationText = "schema "
            + this.schema
            + " with repeatable migration "
            + this.doQuote(migration.getDescription())
            + (canExecuteInTransaction ? "" : " [non-transactional]");
      }

      return migrationText;
   }

   private String doQuote(String text) {
      return "\"" + text + "\"";
   }

   public static class FlywayMigrateException extends FlywayException {
      private final MigrationInfo migration;
      private final boolean executableInTransaction;
      private final boolean outOfOrder;
      private final MigrateErrorResult errorResult;

      FlywayMigrateException(MigrationInfo migration, boolean outOfOrder, SQLException e, boolean canExecuteInTransaction, MigrateResult partialResult) {
         super(ExceptionUtils.toMessage(e), e);
         this.migration = migration;
         this.outOfOrder = outOfOrder;
         this.executableInTransaction = canExecuteInTransaction;
         this.errorResult = new MigrateErrorResult(partialResult, this);
      }

      FlywayMigrateException(MigrationInfo migration, String message, boolean canExecuteInTransaction, MigrateResult partialResult) {
         super(message);
         this.outOfOrder = false;
         this.migration = migration;
         this.executableInTransaction = canExecuteInTransaction;
         this.errorResult = new MigrateErrorResult(partialResult, this);
      }

      FlywayMigrateException(MigrationInfo migration, boolean outOfOrder, FlywayException e, boolean canExecuteInTransaction, MigrateResult partialResult) {
         super(e.getMessage(), e);
         this.migration = migration;
         this.outOfOrder = outOfOrder;
         this.executableInTransaction = canExecuteInTransaction;
         this.errorResult = new MigrateErrorResult(partialResult, this);
      }

      public MigrationInfo getMigration() {
         return this.migration;
      }

      public boolean isExecutableInTransaction() {
         return this.executableInTransaction;
      }

      public boolean isOutOfOrder() {
         return this.outOfOrder;
      }

      public MigrateErrorResult getErrorResult() {
         return this.errorResult;
      }
   }
}
