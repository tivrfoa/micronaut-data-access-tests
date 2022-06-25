package org.flywaydb.core.api.output;

import java.util.List;

public class InfoResult extends OperationResultBase {
   public String schemaVersion;
   public String schemaName;
   public List<InfoOutput> migrations;
   public boolean allSchemasEmpty;

   public InfoResult(String flywayVersion, String database, String schemaVersion, String schemaName, List<InfoOutput> migrations, boolean allSchemasEmpty) {
      this.flywayVersion = flywayVersion;
      this.database = database;
      this.schemaVersion = schemaVersion;
      this.schemaName = schemaName;
      this.migrations = migrations;
      this.operation = "info";
      this.allSchemasEmpty = allSchemasEmpty;
   }
}
