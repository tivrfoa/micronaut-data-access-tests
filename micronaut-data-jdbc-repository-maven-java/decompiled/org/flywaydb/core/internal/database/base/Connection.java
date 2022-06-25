package org.flywaydb.core.internal.database.base;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;

public abstract class Connection<D extends Database> implements Closeable {
   protected final D database;
   protected JdbcTemplate jdbcTemplate;
   private final java.sql.Connection jdbcConnection;
   protected final String originalSchemaNameOrSearchPath;
   private final boolean originalAutoCommit;

   protected Connection(D database, java.sql.Connection connection) {
      this.database = database;

      try {
         this.originalAutoCommit = connection.getAutoCommit();
         if (!this.originalAutoCommit) {
            connection.setAutoCommit(true);
         }
      } catch (SQLException var5) {
         throw new FlywaySqlException("Unable to turn on auto-commit for the connection", var5);
      }

      this.jdbcConnection = connection;
      this.jdbcTemplate = new JdbcTemplate(this.jdbcConnection, database.getDatabaseType());

      try {
         this.originalSchemaNameOrSearchPath = this.getCurrentSchemaNameOrSearchPath();
      } catch (SQLException var4) {
         throw new FlywaySqlException("Unable to determine the original schema for the connection", var4);
      }
   }

   protected abstract String getCurrentSchemaNameOrSearchPath() throws SQLException;

   public final Schema getCurrentSchema() {
      try {
         return this.doGetCurrentSchema();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to determine the current schema for the connection", var2);
      }
   }

   protected Schema doGetCurrentSchema() throws SQLException {
      return this.getSchema(this.getCurrentSchemaNameOrSearchPath());
   }

   public abstract Schema getSchema(String var1);

   public void changeCurrentSchemaTo(Schema schema) {
      try {
         if (schema.exists()) {
            this.doChangeCurrentSchemaOrSearchPathTo(schema.getName());
         }
      } catch (SQLException var3) {
         throw new FlywaySqlException("Error setting current schema to " + schema, var3);
      }
   }

   protected void doChangeCurrentSchemaOrSearchPathTo(String schemaNameOrSearchPath) throws SQLException {
   }

   public <T> T lock(Table table, Callable<T> callable) {
      return ExecutionTemplateFactory.createTableExclusiveExecutionTemplate(this.jdbcTemplate.getConnection(), table, this.database).execute(callable);
   }

   public final JdbcTemplate getJdbcTemplate() {
      return this.jdbcTemplate;
   }

   public final void close() {
      this.restoreOriginalState();
      this.restoreOriginalSchema();
      this.restoreOriginalAutoCommit();
      JdbcUtils.closeConnection(this.jdbcConnection);
   }

   private void restoreOriginalSchema() {
      ExecutionTemplateFactory.createExecutionTemplate(this.jdbcConnection, this.database).execute(() -> {
         try {
            this.doChangeCurrentSchemaOrSearchPathTo(this.originalSchemaNameOrSearchPath);
            return null;
         } catch (SQLException var2) {
            throw new FlywaySqlException("Unable to restore original schema", var2);
         }
      });
   }

   public final void restoreOriginalState() {
      try {
         this.doRestoreOriginalState();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to restore connection to its original state", var2);
      }
   }

   private void restoreOriginalAutoCommit() {
      try {
         this.jdbcConnection.setAutoCommit(this.originalAutoCommit);
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to restore connection to its original auto-commit setting", var2);
      }
   }

   protected void doRestoreOriginalState() throws SQLException {
   }

   public final java.sql.Connection getJdbcConnection() {
      return this.jdbcConnection;
   }
}
