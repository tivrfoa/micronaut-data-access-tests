package org.flywaydb.core.internal.database;

import java.sql.SQLException;
import org.flywaydb.core.internal.util.SqlCallable;

public class DefaultExecutionStrategy implements DatabaseExecutionStrategy {
   @Override
   public <T> T execute(SqlCallable<T> callable) throws SQLException {
      return callable.call();
   }
}
