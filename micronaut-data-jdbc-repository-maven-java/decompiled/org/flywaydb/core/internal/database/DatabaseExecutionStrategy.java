package org.flywaydb.core.internal.database;

import java.sql.SQLException;
import org.flywaydb.core.internal.util.SqlCallable;

public interface DatabaseExecutionStrategy {
   <T> T execute(SqlCallable<T> var1) throws SQLException;
}
