package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;

public interface SqlStatement {
   int getLineNumber();

   String getSql();

   String getDelimiter();

   boolean canExecuteInTransaction();

   Results execute(JdbcTemplate var1);
}
