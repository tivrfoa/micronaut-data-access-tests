package org.flywaydb.core.internal.database.db2;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

public class DB2Connection extends Connection<DB2Database> {
   DB2Connection(DB2Database database, java.sql.Connection connection) {
      super(database, connection);
   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      return this.jdbcTemplate.queryForString("select current_schema from sysibm.sysdummy1");
   }

   @Override
   public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
      this.jdbcTemplate.execute("SET SCHEMA " + this.database.quote(new String[]{schema}));
   }

   @Override
   public Schema getSchema(String name) {
      return new DB2Schema(this.jdbcTemplate, this.database, name);
   }
}
