package org.flywaydb.core.internal.database.informix;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class InformixTable extends Table<InformixDatabase, InformixSchema> {
   InformixTable(JdbcTemplate jdbcTemplate, InformixDatabase database, InformixSchema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TABLE " + this.name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.exists(null, this.schema, this.name, new String[0]);
   }

   @Override
   protected void doLock() throws SQLException {
      this.jdbcTemplate.update("lock table " + this + " in exclusive mode");
   }

   @Override
   public String toString() {
      return this.name;
   }
}
