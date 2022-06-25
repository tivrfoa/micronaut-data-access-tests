package org.flywaydb.core.internal.database.derby;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

public class DerbyConnection extends Connection<DerbyDatabase> {
   DerbyConnection(DerbyDatabase database, java.sql.Connection connection) {
      super(database, connection);
   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      return this.jdbcTemplate.queryForString("SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1");
   }

   @Override
   public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
      this.jdbcTemplate.execute("SET SCHEMA " + this.database.quote(new String[]{schema}));
   }

   @Override
   public Schema getSchema(String name) {
      return new DerbySchema(this.jdbcTemplate, this.database, name);
   }
}
