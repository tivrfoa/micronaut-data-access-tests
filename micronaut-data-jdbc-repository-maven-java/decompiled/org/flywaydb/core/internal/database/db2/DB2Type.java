package org.flywaydb.core.internal.database.db2;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class DB2Type extends Type<DB2Database, DB2Schema> {
   DB2Type(JdbcTemplate jdbcTemplate, DB2Database database, DB2Schema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TYPE " + this.database.quote(new String[]{this.schema.getName(), this.name}));
   }
}
