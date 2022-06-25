package org.flywaydb.core.api.output;

import java.util.ArrayList;
import java.util.List;

public class UndoResult extends OperationResultBase {
   public String initialSchemaVersion;
   public String targetSchemaVersion;
   public String schemaName;
   public List<UndoOutput> undoneMigrations;
   public int migrationsUndone;

   public UndoResult(String flywayVersion, String database, String schemaName) {
      this.flywayVersion = flywayVersion;
      this.database = database;
      this.schemaName = schemaName;
      this.undoneMigrations = new ArrayList();
      this.operation = "undo";
   }
}
