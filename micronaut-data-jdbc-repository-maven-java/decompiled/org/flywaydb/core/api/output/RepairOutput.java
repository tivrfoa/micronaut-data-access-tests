package org.flywaydb.core.api.output;

public class RepairOutput {
   public String version;
   public String description;
   public String filepath;

   public RepairOutput(String version, String description, String filepath) {
      this.version = version;
      this.description = description;
      this.filepath = filepath;
   }
}
