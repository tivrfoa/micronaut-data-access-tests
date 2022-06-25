package org.flywaydb.core.internal.database.postgresql;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

public class PostgreSQLConnection extends Connection<PostgreSQLDatabase> {
   private final String originalRole;

   protected PostgreSQLConnection(PostgreSQLDatabase database, java.sql.Connection connection) {
      super(database, connection);

      try {
         this.originalRole = this.jdbcTemplate.queryForString("SELECT CURRENT_USER");
      } catch (SQLException var4) {
         throw new FlywaySqlException("Unable to determine current user", var4);
      }
   }

   @Override
   protected void doRestoreOriginalState() throws SQLException {
      this.jdbcTemplate.execute("SET ROLE '" + this.originalRole + "'");
   }

   @Override
   public Schema doGetCurrentSchema() throws SQLException {
      String currentSchema = this.jdbcTemplate.queryForString("SELECT current_schema");
      String searchPath = this.getCurrentSchemaNameOrSearchPath();
      if (!StringUtils.hasText(currentSchema) && !StringUtils.hasText(searchPath)) {
         throw new FlywayException(
            "Unable to determine current schema as search_path is empty. Set the current schema in currentSchema parameter of the JDBC URL or in Flyway's schemas property."
         );
      } else {
         String schema = StringUtils.hasText(currentSchema) ? currentSchema : searchPath;
         return this.getSchema(schema);
      }
   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      return this.jdbcTemplate.queryForString("SHOW search_path");
   }

   @Override
   public void changeCurrentSchemaTo(Schema schema) {
      try {
         if (!schema.getName().equals(this.originalSchemaNameOrSearchPath)
            && !this.originalSchemaNameOrSearchPath.startsWith(schema.getName() + ",")
            && schema.exists()) {
            if (StringUtils.hasText(this.originalSchemaNameOrSearchPath)) {
               this.doChangeCurrentSchemaOrSearchPathTo(schema + "," + this.originalSchemaNameOrSearchPath);
            } else {
               this.doChangeCurrentSchemaOrSearchPathTo(schema.toString());
            }

         }
      } catch (SQLException var3) {
         throw new FlywaySqlException("Error setting current schema to " + schema, var3);
      }
   }

   @Override
   public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
      this.jdbcTemplate.execute("SELECT set_config('search_path', ?, false)", schema);
   }

   @Override
   public Schema getSchema(String name) {
      return new PostgreSQLSchema(this.jdbcTemplate, this.database, name);
   }

   @Override
   public <T> T lock(Table table, Callable<T> callable) {
      return new PostgreSQLAdvisoryLockTemplate(this.jdbcTemplate, table.toString().hashCode()).execute(callable);
   }
}
