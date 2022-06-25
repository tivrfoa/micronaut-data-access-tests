package org.flywaydb.core.internal.license;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.DatabaseType;

public class FlywayEditionUpgradeRequiredException extends FlywayException {
   public FlywayEditionUpgradeRequiredException(Edition edition, DatabaseType databaseType, String version) {
      super(
         edition
            + " or "
            + databaseType.getName()
            + " upgrade required: "
            + databaseType.getName()
            + " "
            + version
            + " is no longer supported by "
            + VersionPrinter.EDITION
            + ", but still supported by "
            + edition
            + "."
      );
   }

   public FlywayEditionUpgradeRequiredException(Edition required, Edition current, String feature) {
      super(required + " upgrade required: " + feature + " is not supported by " + current + ".");
   }
}
