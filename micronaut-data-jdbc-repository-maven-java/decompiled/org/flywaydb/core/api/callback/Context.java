package org.flywaydb.core.api.callback;

import java.sql.Connection;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;

public interface Context {
   Configuration getConfiguration();

   Connection getConnection();

   MigrationInfo getMigrationInfo();

   Statement getStatement();

   OperationResult getOperationResult();
}
