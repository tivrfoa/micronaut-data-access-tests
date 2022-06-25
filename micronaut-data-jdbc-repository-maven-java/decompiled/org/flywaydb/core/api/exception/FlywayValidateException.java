package org.flywaydb.core.api.exception;

import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.FlywayException;

public class FlywayValidateException extends FlywayException {
   public FlywayValidateException(ErrorDetails errorDetails, String allValidateMessages) {
      super(
         "Validate failed: "
            + errorDetails.errorMessage
            + "\n"
            + allValidateMessages
            + "\nNeed more flexibility with validation rules? Learn more: "
            + "https://rd.gt/3AbJUZE",
         errorDetails.errorCode
      );
   }
}
