package org.flywaydb.core.api.output;

import org.flywaydb.core.api.ErrorDetails;

public class ValidateOutput {
   public final String version;
   public final String description;
   public final String filepath;
   public final ErrorDetails errorDetails;

   public ValidateOutput(String version, String description, String filepath, ErrorDetails errorDetails) {
      this.version = version;
      this.description = description;
      this.filepath = filepath;
      this.errorDetails = errorDetails;
   }
}
