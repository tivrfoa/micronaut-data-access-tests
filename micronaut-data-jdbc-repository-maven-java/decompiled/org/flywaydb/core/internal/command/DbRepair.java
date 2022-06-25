package org.flywaydb.core.internal.command;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.RepairResult;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;

public class DbRepair {
   private static final Log LOG = LogFactory.getLog(DbRepair.class);
   private final Connection connection;
   private final MigrationInfoServiceImpl migrationInfoService;
   private final SchemaHistory schemaHistory;
   private final CallbackExecutor callbackExecutor;
   private final Database database;
   private RepairResult repairResult;
   private final Configuration configuration;

   public DbRepair(
      Database database, MigrationResolver migrationResolver, SchemaHistory schemaHistory, CallbackExecutor callbackExecutor, Configuration configuration
   ) {
      this.database = database;
      this.connection = database.getMainConnection();
      this.schemaHistory = schemaHistory;
      this.callbackExecutor = callbackExecutor;
      this.configuration = configuration;
      this.migrationInfoService = new MigrationInfoServiceImpl(
         migrationResolver, schemaHistory, database, configuration, MigrationVersion.LATEST, true, configuration.getCherryPick(), true, true, true, true
      );
      this.repairResult = CommandResultFactory.createRepairResult(database.getCatalog());
   }

   public RepairResult repair() {
      this.callbackExecutor.onEvent(Event.BEFORE_REPAIR);

      DbRepair.CompletedRepairActions repairActions;
      try {
         StopWatch stopWatch = new StopWatch();
         stopWatch.start();
         repairActions = ExecutionTemplateFactory.createExecutionTemplate(this.connection.getJdbcConnection(), this.database)
            .execute(
               new Callable<DbRepair.CompletedRepairActions>() {
                  public DbRepair.CompletedRepairActions call() {
                     DbRepair.CompletedRepairActions completedActions = new DbRepair.CompletedRepairActions();
                     completedActions.removedFailedMigrations = DbRepair.this.schemaHistory
                        .removeFailedMigrations(DbRepair.this.repairResult, DbRepair.this.configuration.getCherryPick());
                     DbRepair.this.migrationInfoService.refresh();
                     completedActions.deletedMissingMigrations = DbRepair.this.deleteMissingMigrations();
                     completedActions.alignedAppliedMigrationChecksums = DbRepair.this.alignAppliedMigrationsWithResolvedMigrations();
                     return completedActions;
                  }
               }
            );
         stopWatch.stop();
         LOG.info(
            "Successfully repaired schema history table " + this.schemaHistory + " (execution time " + TimeFormat.format(stopWatch.getTotalTimeMillis()) + ")."
         );
         if (repairActions.deletedMissingMigrations) {
            LOG.info("Please ensure the previous contents of the deleted migrations are removed from the database, or moved into an existing migration.");
         }

         if (repairActions.removedFailedMigrations && !this.database.supportsDdlTransactions()) {
            LOG.info("Manual cleanup of the remaining effects of the failed migration may still be required.");
         }
      } catch (FlywayException var3) {
         this.callbackExecutor.onEvent(Event.AFTER_REPAIR_ERROR);
         throw var3;
      }

      this.callbackExecutor.onEvent(Event.AFTER_REPAIR);
      this.repairResult.setRepairActions(repairActions);
      return this.repairResult;
   }

   private boolean deleteMissingMigrations() {
      boolean removed = false;

      for(MigrationInfo migrationInfo : this.migrationInfoService.all()) {
         MigrationInfoImpl migrationInfoImpl = (MigrationInfoImpl)migrationInfo;
         if (!migrationInfo.getType().isSynthetic()) {
            AppliedMigration applied = migrationInfoImpl.getAppliedMigration();
            MigrationState state = migrationInfoImpl.getState();
            boolean isMigrationMissing = state == MigrationState.MISSING_SUCCESS
               || state == MigrationState.MISSING_FAILED
               || state == MigrationState.FUTURE_SUCCESS
               || state == MigrationState.FUTURE_FAILED;
            boolean isMigrationIgnored = Arrays.stream(this.configuration.getIgnoreMigrationPatterns())
               .anyMatch(p -> p.matchesMigration(migrationInfoImpl.getVersion() != null, state));
            if (isMigrationMissing && !isMigrationIgnored) {
               this.schemaHistory.delete(applied);
               removed = true;
               this.repairResult.migrationsDeleted.add(CommandResultFactory.createRepairOutput(migrationInfo));
            }
         }
      }

      return removed;
   }

   private boolean alignAppliedMigrationsWithResolvedMigrations() {
      boolean repaired = false;

      for(MigrationInfo migrationInfo : this.migrationInfoService.all()) {
         MigrationInfoImpl migrationInfoImpl = (MigrationInfoImpl)migrationInfo;
         ResolvedMigration resolved = migrationInfoImpl.getResolvedMigration();
         AppliedMigration applied = migrationInfoImpl.getAppliedMigration();
         if (resolved != null
            && resolved.getVersion() != null
            && applied != null
            && !applied.getType().isSynthetic()
            && migrationInfoImpl.getState() != MigrationState.IGNORED
            && this.updateNeeded(resolved, applied)) {
            this.schemaHistory.update(applied, resolved);
            repaired = true;
            this.repairResult.migrationsAligned.add(CommandResultFactory.createRepairOutput(migrationInfo));
         }

         if (resolved != null
            && resolved.getVersion() == null
            && applied != null
            && !applied.getType().isSynthetic()
            && migrationInfoImpl.getState() != MigrationState.IGNORED
            && resolved.checksumMatchesWithoutBeingIdentical(applied.getChecksum())) {
            this.schemaHistory.update(applied, resolved);
            repaired = true;
            this.repairResult.migrationsAligned.add(CommandResultFactory.createRepairOutput(migrationInfo));
         }
      }

      return repaired;
   }

   private boolean updateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
      return this.checksumUpdateNeeded(resolved, applied) || this.descriptionUpdateNeeded(resolved, applied) || this.typeUpdateNeeded(resolved, applied);
   }

   private boolean checksumUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
      return !resolved.checksumMatches(applied.getChecksum());
   }

   private boolean descriptionUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
      if (!this.database.supportsEmptyMigrationDescription() && "".equals(resolved.getDescription())) {
         return !Objects.equals("<< no description >>", applied.getDescription());
      } else {
         return !Objects.equals(resolved.getDescription(), applied.getDescription());
      }
   }

   private boolean typeUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
      return !Objects.equals(resolved.getType(), applied.getType());
   }

   public static class CompletedRepairActions {
      public boolean removedFailedMigrations = false;
      public boolean deletedMissingMigrations = false;
      public boolean alignedAppliedMigrationChecksums = false;

      public String removedMessage() {
         return "Removed failed migrations";
      }

      public String deletedMessage() {
         return "Deleted missing migrations";
      }

      public String alignedMessage() {
         return "Aligned applied migration checksums";
      }
   }
}
