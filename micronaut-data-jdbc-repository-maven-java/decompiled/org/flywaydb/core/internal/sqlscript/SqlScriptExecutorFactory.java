package org.flywaydb.core.internal.sqlscript;

import java.sql.Connection;

public interface SqlScriptExecutorFactory {
   SqlScriptExecutor createSqlScriptExecutor(Connection var1, boolean var2, boolean var3, boolean var4);
}
