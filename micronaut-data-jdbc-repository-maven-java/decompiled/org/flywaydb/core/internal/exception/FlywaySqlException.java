package org.flywaydb.core.internal.exception;

import java.sql.SQLException;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.StringUtils;

public class FlywaySqlException extends FlywayException {
   public FlywaySqlException(String message, SQLException sqlException) {
      super(message, sqlException, ErrorCode.DB_CONNECTION);
   }

   public String getMessage() {
      String title = super.getMessage();
      String underline = StringUtils.trimOrPad("", title.length(), '-');
      return title + "\n" + underline + "\n" + ExceptionUtils.toMessage((SQLException)this.getCause());
   }
}
