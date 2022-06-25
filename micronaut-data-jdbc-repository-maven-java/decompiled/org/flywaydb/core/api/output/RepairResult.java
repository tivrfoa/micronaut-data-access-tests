package org.flywaydb.core.api.output;

import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.internal.command.DbRepair;

public class RepairResult extends OperationResultBase {
   public List<String> repairActions;
   public List<RepairOutput> migrationsRemoved;
   public List<RepairOutput> migrationsDeleted;
   public List<RepairOutput> migrationsAligned;

   public RepairResult(String flywayVersion, String database) {
      this.flywayVersion = flywayVersion;
      this.database = database;
      this.repairActions = new ArrayList();
      this.migrationsRemoved = new ArrayList();
      this.migrationsDeleted = new ArrayList();
      this.migrationsAligned = new ArrayList();
      this.operation = "repair";
   }

   public void setRepairActions(DbRepair.CompletedRepairActions completedRepairActions) {
      if (completedRepairActions.removedFailedMigrations) {
         this.repairActions.add(completedRepairActions.removedMessage());
      }

      if (completedRepairActions.deletedMissingMigrations) {
         this.repairActions.add(completedRepairActions.deletedMessage());
      }

      if (completedRepairActions.alignedAppliedMigrationChecksums) {
         this.repairActions.add(completedRepairActions.alignedMessage());
      }

   }
}
