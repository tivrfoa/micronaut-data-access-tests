package org.flywaydb.core.api.output;

import java.util.LinkedList;
import java.util.List;

public abstract class OperationResultBase implements OperationResult {
   public String flywayVersion;
   public String database;
   public List<String> warnings = new LinkedList();
   public String operation;

   public void addWarning(String warning) {
      this.warnings.add(warning);
   }
}
