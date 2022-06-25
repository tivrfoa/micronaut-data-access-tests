package org.flywaydb.core.api.output;

import java.util.ArrayList;
import java.util.List;

public class MigrateResult extends OperationResultBase {
   public String initialSchemaVersion;
   public String targetSchemaVersion;
   public String schemaName;
   public List<MigrateOutput> migrations;
   public int migrationsExecuted;
   public boolean success;

   public MigrateResult(String flywayVersion, String database, String schemaName) {
      this.flywayVersion = flywayVersion;
      this.database = database;
      this.schemaName = schemaName;
      this.migrations = new ArrayList();
      this.operation = "migrate";
      this.success = true;
   }

   MigrateResult(MigrateResult migrateResult) {
      this.flywayVersion = migrateResult.flywayVersion;
      this.database = migrateResult.database;
      this.schemaName = migrateResult.schemaName;
      this.migrations = migrateResult.migrations;
      this.operation = migrateResult.operation;
      this.success = migrateResult.success;
      this.migrationsExecuted = migrateResult.migrationsExecuted;
      this.initialSchemaVersion = migrateResult.initialSchemaVersion;
      this.targetSchemaVersion = migrateResult.targetSchemaVersion;
      this.warnings = migrateResult.warnings;
   }
}
