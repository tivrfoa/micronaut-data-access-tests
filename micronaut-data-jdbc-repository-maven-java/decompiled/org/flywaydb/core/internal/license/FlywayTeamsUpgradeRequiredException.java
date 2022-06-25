package org.flywaydb.core.internal.license;

import org.flywaydb.core.api.FlywayException;

public class FlywayTeamsUpgradeRequiredException extends FlywayException {
   public FlywayTeamsUpgradeRequiredException(String feature) {
      super(
         Edition.ENTERPRISE
            + " upgrade required: "
            + feature
            + " is not supported by "
            + Edition.COMMUNITY
            + "\nTry "
            + Edition.ENTERPRISE
            + " for free: "
            + "https://rd.gt/2VzHpkY"
      );
   }
}
