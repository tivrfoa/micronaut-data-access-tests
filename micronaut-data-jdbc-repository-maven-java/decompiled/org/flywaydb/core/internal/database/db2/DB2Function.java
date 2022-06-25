package org.flywaydb.core.internal.database.db2;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Function;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class DB2Function extends Function {
   DB2Function(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name, String... args) {
      super(jdbcTemplate, database, schema, name, args);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP SPECIFIC FUNCTION " + this.database.quote(this.schema.getName(), this.name));
   }
}
