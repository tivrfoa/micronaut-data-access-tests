package org.flywaydb.core.internal.sqlscript;

import java.sql.SQLException;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.internal.exception.FlywaySqlException;

public class FlywaySqlScriptException extends FlywaySqlException {
   private final Resource resource;
   private final SqlStatement statement;

   public FlywaySqlScriptException(Resource resource, SqlStatement statement, SQLException sqlException) {
      super(resource == null ? "Script failed" : "Migration " + resource.getFilename() + " failed", sqlException);
      this.resource = resource;
      this.statement = statement;
   }

   public int getLineNumber() {
      return this.statement == null ? -1 : this.statement.getLineNumber();
   }

   public String getStatement() {
      return this.statement == null ? "" : this.statement.getSql();
   }

   @Override
   public String getMessage() {
      String message = super.getMessage();
      if (this.resource != null) {
         message = message + "Location   : " + this.resource.getAbsolutePath() + " (" + this.resource.getAbsolutePathOnDisk() + ")\n";
      }

      if (this.statement != null) {
         message = message + "Line       : " + this.getLineNumber() + "\n";
         message = message + "Statement  : " + this.getStatement() + "\n";
      }

      return message;
   }

   public Resource getResource() {
      return this.resource;
   }
}
