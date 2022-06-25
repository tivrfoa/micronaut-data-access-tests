package org.flywaydb.core.internal.database.oracle;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

public class OracleConnection extends Connection<OracleDatabase> {
   OracleConnection(OracleDatabase database, java.sql.Connection connection) {
      super(database, connection);
   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      return this.jdbcTemplate.queryForString("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL");
   }

   @Override
   public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
      this.jdbcTemplate.execute("ALTER SESSION SET CURRENT_SCHEMA=" + this.database.quote(new String[]{schema}));
   }

   @Override
   public Schema getSchema(String name) {
      return new OracleSchema(this.jdbcTemplate, this.database, name);
   }
}
