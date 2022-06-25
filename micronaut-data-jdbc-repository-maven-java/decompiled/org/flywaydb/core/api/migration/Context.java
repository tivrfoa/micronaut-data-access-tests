package org.flywaydb.core.api.migration;

import java.sql.Connection;
import org.flywaydb.core.api.configuration.Configuration;

public interface Context {
   Configuration getConfiguration();

   Connection getConnection();
}
