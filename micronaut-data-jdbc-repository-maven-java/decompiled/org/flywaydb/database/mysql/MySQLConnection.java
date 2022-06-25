package org.flywaydb.database.mysql;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

public class MySQLConnection extends Connection<MySQLDatabase> {
   private static final Log LOG = LogFactory.getLog(MySQLConnection.class);
   private static final String USER_VARIABLES_TABLE_MARIADB = "information_schema.user_variables";
   private static final String USER_VARIABLES_TABLE_MYSQL = "performance_schema.user_variables_by_thread";
   private static final String FOREIGN_KEY_CHECKS = "foreign_key_checks";
   private static final String SQL_SAFE_UPDATES = "sql_safe_updates";
   private final String userVariablesQuery;
   private final boolean canResetUserVariables;
   private final int originalForeignKeyChecks;
   private final int originalSqlSafeUpdates;

   public MySQLConnection(MySQLDatabase database, java.sql.Connection connection) {
      super(database, connection);
      this.userVariablesQuery = "SELECT variable_name FROM "
         + (database.isMariaDB() ? "information_schema.user_variables" : "performance_schema.user_variables_by_thread")
         + " WHERE variable_value IS NOT NULL";
      this.canResetUserVariables = this.hasUserVariableResetCapability();
      this.originalForeignKeyChecks = this.getIntVariableValue("foreign_key_checks");
      this.originalSqlSafeUpdates = this.getIntVariableValue("sql_safe_updates");
   }

   private int getIntVariableValue(String varName) {
      try {
         return this.jdbcTemplate.queryForInt("SELECT @@" + varName);
      } catch (SQLException var3) {
         throw new FlywaySqlException("Unable to determine value for '" + varName + "' variable", var3);
      }
   }

   private boolean hasUserVariableResetCapability() {
      try {
         this.jdbcTemplate.queryForStringList(this.userVariablesQuery);
         return true;
      } catch (SQLException var2) {
         LOG.debug(
            "Disabled user variable reset as "
               + (this.database.isMariaDB() ? "information_schema.user_variables" : "performance_schema.user_variables_by_thread")
               + "cannot be queried (SQL State: "
               + var2.getSQLState()
               + ", Error Code: "
               + var2.getErrorCode()
               + ")"
         );
         return false;
      }
   }

   @Override
   protected void doRestoreOriginalState() throws SQLException {
      this.resetUserVariables();
      this.jdbcTemplate.execute("SET foreign_key_checks=?, sql_safe_updates=?", this.originalForeignKeyChecks, this.originalSqlSafeUpdates);
   }

   private void resetUserVariables() throws SQLException {
      if (this.canResetUserVariables) {
         List<String> userVariables = this.jdbcTemplate.queryForStringList(this.userVariablesQuery);
         if (!userVariables.isEmpty()) {
            boolean first = true;
            StringBuilder setStatement = new StringBuilder("SET ");

            for(String userVariable : userVariables) {
               if (first) {
                  first = false;
               } else {
                  setStatement.append(",");
               }

               setStatement.append("@").append(userVariable).append("=NULL");
            }

            this.jdbcTemplate.executeStatement(setStatement.toString());
         }
      }

   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      return this.jdbcTemplate.queryForString("SELECT DATABASE()");
   }

   @Override
   public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
      if (StringUtils.hasLength(schema)) {
         this.jdbcTemplate.getConnection().setCatalog(schema);
      } else {
         try {
            String newDb = this.database.quote(new String[]{UUID.randomUUID().toString()});
            this.jdbcTemplate.execute("CREATE SCHEMA " + newDb);
            this.jdbcTemplate.execute("USE " + newDb);
            this.jdbcTemplate.execute("DROP SCHEMA " + newDb);
         } catch (Exception var3) {
            LOG.warn("Unable to restore connection to having no default schema: " + var3.getMessage());
         }
      }

   }

   @Override
   protected Schema doGetCurrentSchema() throws SQLException {
      String schemaName = this.getCurrentSchemaNameOrSearchPath();
      return schemaName == null ? null : this.getSchema(schemaName);
   }

   @Override
   public Schema getSchema(String name) {
      return new MySQLSchema(this.jdbcTemplate, this.database, name);
   }

   @Override
   public <T> T lock(Table table, Callable<T> callable) {
      return (T)(this.canUseNamedLockTemplate()
         ? new MySQLNamedLockTemplate(this.jdbcTemplate, table.toString().hashCode()).execute(callable)
         : super.lock(table, callable));
   }

   protected boolean canUseNamedLockTemplate() {
      return !this.database.isPxcStrict();
   }
}
