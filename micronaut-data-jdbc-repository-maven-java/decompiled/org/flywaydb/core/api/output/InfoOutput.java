package org.flywaydb.core.api.output;

public class InfoOutput {
   public String category;
   public String version;
   public String description;
   public String type;
   public String installedOnUTC;
   public String state;
   public String undoable;
   public String filepath;
   public String undoFilepath;
   public String installedBy;
   public int executionTime;

   public InfoOutput(
      String category,
      String version,
      String description,
      String type,
      String installedOnUTC,
      String state,
      String undoable,
      String filepath,
      String undoFilepath,
      String installedBy,
      int executionTime
   ) {
      this.category = category;
      this.version = version;
      this.description = description;
      this.type = type;
      this.installedOnUTC = installedOnUTC;
      this.state = state;
      this.undoable = undoable;
      this.filepath = filepath;
      this.undoFilepath = undoFilepath;
      this.installedBy = installedBy;
      this.executionTime = executionTime;
   }
}
