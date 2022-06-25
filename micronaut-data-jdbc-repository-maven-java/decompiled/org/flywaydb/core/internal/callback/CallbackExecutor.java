package org.flywaydb.core.internal.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.output.OperationResult;

public interface CallbackExecutor {
   void onEvent(Event var1);

   void onMigrateOrUndoEvent(Event var1);

   void setMigrationInfo(MigrationInfo var1);

   void onEachMigrateOrUndoEvent(Event var1);

   void onOperationFinishEvent(Event var1, OperationResult var2);
}
