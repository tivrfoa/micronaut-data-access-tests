package org.flywaydb.core.internal.database.h2;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

public class H2Connection extends Connection<H2Database> {
   private final boolean requiresV2Metadata;

   H2Connection(H2Database database, java.sql.Connection connection, boolean requiresV2Metadata) {
      super(database, connection);
      this.requiresV2Metadata = requiresV2Metadata;
   }

   @Override
   public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
      this.jdbcTemplate.execute("SET SCHEMA " + this.database.quote(new String[]{schema}));
   }

   @Override
   public Schema getSchema(String name) {
      return new H2Schema(this.jdbcTemplate, this.database, name, this.requiresV2Metadata);
   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      return this.jdbcTemplate.queryForString("CALL SCHEMA()");
   }
}
