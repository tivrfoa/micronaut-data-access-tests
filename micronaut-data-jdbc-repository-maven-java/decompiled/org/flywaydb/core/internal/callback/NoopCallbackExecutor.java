package org.flywaydb.core.internal.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.output.OperationResult;

public enum NoopCallbackExecutor implements CallbackExecutor {
   INSTANCE;

   @Override
   public void onEvent(Event event) {
   }

   @Override
   public void onMigrateOrUndoEvent(Event event) {
   }

   @Override
   public void setMigrationInfo(MigrationInfo migrationInfo) {
   }

   @Override
   public void onEachMigrateOrUndoEvent(Event event) {
   }

   @Override
   public void onOperationFinishEvent(Event event, OperationResult operationResult) {
   }
}
