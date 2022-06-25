package org.flywaydb.core.api.output;

public class UndoOutput {
   public String version;
   public String description;
   public String filepath;
   public int executionTime;

   public UndoOutput(String version, String description, String filepath, int executionTime) {
      this.version = version;
      this.description = description;
      this.filepath = filepath;
      this.executionTime = executionTime;
   }
}
