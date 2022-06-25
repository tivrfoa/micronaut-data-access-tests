package org.flywaydb.core.internal.database.db2;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class DB2Table extends Table<DB2Database, DB2Schema> {
   DB2Table(JdbcTemplate jdbcTemplate, DB2Database database, DB2Schema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TABLE " + this);
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.exists(null, this.schema, this.name, new String[0]);
   }

   @Override
   protected void doLock() throws SQLException {
      this.jdbcTemplate.update("lock table " + this + " in exclusive mode");
   }
}
