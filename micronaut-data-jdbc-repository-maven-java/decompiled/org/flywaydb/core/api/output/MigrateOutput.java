package org.flywaydb.core.api.output;

public class MigrateOutput {
   public String category;
   public String version;
   public String description;
   public String type;
   public String filepath;
   public int executionTime;

   public MigrateOutput(String category, String version, String description, String type, String filepath, int executionTime) {
      this.category = category;
      this.version = version;
      this.description = description;
      this.type = type;
      this.filepath = filepath;
      this.executionTime = executionTime;
   }
}
