package org.flywaydb.core.internal.license;

import org.flywaydb.core.api.FlywayException;

public class FlywayTrialExpiredException extends FlywayException {
   public FlywayTrialExpiredException(Edition edition) {
      super(
         "Your 30 day limited Flyway trial license has expired and is no longer valid. Visit https://rd.gt/2WNixqj to upgrade to a full "
            + edition
            + " license to keep on using this software."
      );
   }
}
