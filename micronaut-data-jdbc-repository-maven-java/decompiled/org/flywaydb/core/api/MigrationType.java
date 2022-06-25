package org.flywaydb.core.api;

public enum MigrationType {
   SCHEMA(true, false, false),
   BASELINE(true, false, false),
   DELETE(true, false, false),
   SQL(false, false, false),
   SQL_BASELINE(false, false, true),
   UNDO_SQL(false, true, false),
   JDBC(false, false, false),
   JDBC_BASELINE(false, false, true),
   UNDO_JDBC(false, true, false),
   SCRIPT(false, false, false),
   SCRIPT_BASELINE(false, false, true),
   UNDO_SCRIPT(false, true, false),
   CUSTOM(false, false, false),
   UNDO_CUSTOM(false, true, false);

   private final boolean synthetic;
   private final boolean undo;
   private final boolean baselineMigration;

   public static MigrationType fromString(String migrationType) {
      if ("SPRING_JDBC".equals(migrationType)) {
         return JDBC;
      } else if ("UNDO_SPRING_JDBC".equals(migrationType)) {
         return UNDO_JDBC;
      } else if ("SQL_STATE_SCRIPT".equals(migrationType)) {
         return SQL_BASELINE;
      } else {
         return "JDBC_STATE_SCRIPT".equals(migrationType) ? JDBC_BASELINE : valueOf(migrationType);
      }
   }

   private MigrationType(boolean synthetic, boolean undo, boolean baselineMigration) {
      this.synthetic = synthetic;
      this.undo = undo;
      this.baselineMigration = baselineMigration;
   }

   public boolean isSynthetic() {
      return this.synthetic;
   }

   public boolean isUndo() {
      return this.undo;
   }

   public boolean isBaselineMigration() {
      return this.baselineMigration;
   }
}
