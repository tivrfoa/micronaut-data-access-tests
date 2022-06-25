package org.flywaydb.core.internal.database.snowflake;

import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

public class SnowflakeConnection extends Connection<SnowflakeDatabase> {
   private final String originalRole;

   SnowflakeConnection(SnowflakeDatabase database, java.sql.Connection connection) {
      super(database, connection);

      try {
         this.originalRole = this.jdbcTemplate.queryForString("SELECT CURRENT_ROLE()");
      } catch (SQLException var4) {
         throw new FlywayException("Unable to determine current role", var4);
      }
   }

   @Override
   protected void doRestoreOriginalState() throws SQLException {
      this.jdbcTemplate.execute("USE ROLE " + this.database.doQuote(this.originalRole));
   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      String schemaName = this.jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA()");
      return schemaName != null ? schemaName : "PUBLIC";
   }

   @Override
   public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
      this.jdbcTemplate.execute("USE SCHEMA " + this.database.doQuote(schema));
   }

   @Override
   public Schema getSchema(String name) {
      return new SnowflakeSchema(this.jdbcTemplate, this.database, name);
   }
}
