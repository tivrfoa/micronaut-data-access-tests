package org.flywaydb.core.internal.database.informix;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

public class InformixConnection extends Connection<InformixDatabase> {
   InformixConnection(InformixDatabase database, java.sql.Connection connection) {
      super(database, connection);
   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      return this.getJdbcConnection().getMetaData().getUserName();
   }

   @Override
   public Schema getSchema(String name) {
      return new InformixSchema(this.jdbcTemplate, this.database, name);
   }

   @Override
   public void changeCurrentSchemaTo(Schema schema) {
   }
}
