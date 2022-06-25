package org.flywaydb.core.internal.database;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.ExecutionTemplate;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

public interface DatabaseType extends Plugin, Comparable<DatabaseType> {
   String getName();

   int getNullType();

   boolean handlesJDBCUrl(String var1);

   int getPriority();

   int compareTo(DatabaseType var1);

   Pattern getJDBCCredentialsPattern();

   String getDriverClass(String var1, ClassLoader var2);

   String getBackupDriverClass(String var1, ClassLoader var2);

   boolean handlesDatabaseProductNameAndVersion(String var1, String var2, Connection var3);

   Database createDatabase(Configuration var1, boolean var2, JdbcConnectionFactory var3, StatementInterceptor var4);

   Database createDatabase(Configuration var1, JdbcConnectionFactory var2, StatementInterceptor var3);

   Parser createParser(Configuration var1, ResourceProvider var2, ParsingContext var3);

   SqlScriptFactory createSqlScriptFactory(Configuration var1, ParsingContext var2);

   SqlScriptExecutorFactory createSqlScriptExecutorFactory(JdbcConnectionFactory var1, CallbackExecutor var2, StatementInterceptor var3);

   DatabaseExecutionStrategy createExecutionStrategy(Connection var1);

   ExecutionTemplate createTransactionalExecutionTemplate(Connection var1, boolean var2);

   void setDefaultConnectionProps(String var1, Properties var2, ClassLoader var3);

   void setConfigConnectionProps(Configuration var1, Properties var2, ClassLoader var3);

   void setOverridingConnectionProps(Map<String, String> var1);

   void shutdownDatabase(String var1, Driver var2);

   boolean detectUserRequiredByUrl(String var1);

   boolean detectPasswordRequiredByUrl(String var1);

   boolean externalAuthPropertiesRequired(String var1, String var2, String var3);

   Properties getExternalAuthProperties(String var1, String var2);

   Connection alterConnectionAsNeeded(Connection var1, Configuration var2);

   String instantiateClassExtendedErrorMessage();

   void printMessages();
}
