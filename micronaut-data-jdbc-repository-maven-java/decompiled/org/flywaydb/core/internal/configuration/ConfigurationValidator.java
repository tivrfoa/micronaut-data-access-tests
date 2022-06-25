package org.flywaydb.core.internal.configuration;

import java.util.Locale;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;

public class ConfigurationValidator {
   public void validate(Configuration configuration) {
      if (configuration.getDataSource() == null) {
         throw new FlywayException("Unable to connect to the database. Configure the url, user and password!", ErrorCode.CONFIGURATION);
      } else {
         for(String key : configuration.getPlaceholders().keySet()) {
            if (key.toLowerCase(Locale.ENGLISH).startsWith("flyway:")) {
               throw new FlywayException("Invalid placeholder ('flyway:' prefix is reserved): " + key);
            }
         }

      }
   }
}
