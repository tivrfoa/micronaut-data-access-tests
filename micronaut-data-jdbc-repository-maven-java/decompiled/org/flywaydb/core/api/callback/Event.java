package org.flywaydb.core.api.callback;

public enum Event {
   BEFORE_CLEAN("beforeClean"),
   AFTER_CLEAN("afterClean"),
   AFTER_CLEAN_ERROR("afterCleanError"),
   BEFORE_MIGRATE("beforeMigrate"),
   BEFORE_EACH_MIGRATE("beforeEachMigrate"),
   BEFORE_EACH_MIGRATE_STATEMENT("beforeEachMigrateStatement"),
   AFTER_EACH_MIGRATE_STATEMENT("afterEachMigrateStatement"),
   AFTER_EACH_MIGRATE_STATEMENT_ERROR("afterEachMigrateStatementError"),
   AFTER_EACH_MIGRATE("afterEachMigrate"),
   AFTER_EACH_MIGRATE_ERROR("afterEachMigrateError"),
   BEFORE_REPEATABLES("beforeRepeatables"),
   AFTER_VERSIONED("afterVersioned"),
   AFTER_MIGRATE_APPLIED("afterMigrateApplied"),
   AFTER_MIGRATE("afterMigrate"),
   AFTER_MIGRATE_ERROR("afterMigrateError"),
   BEFORE_UNDO("beforeUndo"),
   BEFORE_EACH_UNDO("beforeEachUndo"),
   BEFORE_EACH_UNDO_STATEMENT("beforeEachUndoStatement"),
   AFTER_EACH_UNDO_STATEMENT("afterEachUndoStatement"),
   AFTER_EACH_UNDO_STATEMENT_ERROR("afterEachUndoStatementError"),
   AFTER_EACH_UNDO("afterEachUndo"),
   AFTER_EACH_UNDO_ERROR("afterEachUndoError"),
   AFTER_UNDO("afterUndo"),
   AFTER_UNDO_ERROR("afterUndoError"),
   BEFORE_VALIDATE("beforeValidate"),
   AFTER_VALIDATE("afterValidate"),
   AFTER_VALIDATE_ERROR("afterValidateError"),
   BEFORE_BASELINE("beforeBaseline"),
   AFTER_BASELINE("afterBaseline"),
   AFTER_BASELINE_ERROR("afterBaselineError"),
   BEFORE_REPAIR("beforeRepair"),
   AFTER_REPAIR("afterRepair"),
   AFTER_REPAIR_ERROR("afterRepairError"),
   BEFORE_INFO("beforeInfo"),
   AFTER_INFO("afterInfo"),
   AFTER_INFO_ERROR("afterInfoError"),
   AFTER_MIGRATE_OPERATION_FINISH("afterMigrateOperationFinish"),
   AFTER_INFO_OPERATION_FINISH("afterInfoOperationFinish"),
   AFTER_CLEAN_OPERATION_FINISH("afterInfoOperationFinish"),
   AFTER_VALIDATE_OPERATION_FINISH("afterInfoOperationFinish"),
   AFTER_UNDO_OPERATION_FINISH("afterInfoOperationFinish"),
   AFTER_REPAIR_OPERATION_FINISH("afterInfoOperationFinish"),
   AFTER_BASELINE_OPERATION_FINISH("afterInfoOperationFinish"),
   CREATE_SCHEMA("createSchema"),
   BEFORE_CONNECT("beforeConnect");

   private final String id;

   public static Event fromId(String id) {
      for(Event event : values()) {
         if (event.id.equals(id)) {
            return event;
         }
      }

      return null;
   }

   public String toString() {
      return this.id;
   }

   private Event(String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }
}
