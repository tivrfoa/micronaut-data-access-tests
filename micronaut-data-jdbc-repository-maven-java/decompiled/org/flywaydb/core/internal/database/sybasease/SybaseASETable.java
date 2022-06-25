package org.flywaydb.core.internal.database.sybasease;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class SybaseASETable extends Table<SybaseASEDatabase, SybaseASESchema> {
   SybaseASETable(JdbcTemplate jdbcTemplate, SybaseASEDatabase database, SybaseASESchema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.jdbcTemplate.queryForString("SELECT object_id('" + this.name + "')") != null;
   }

   @Override
   protected void doLock() throws SQLException {
      if (this.database.supportsMultiStatementTransactions()) {
         this.jdbcTemplate.execute("LOCK TABLE " + this + " IN EXCLUSIVE MODE");
      }

   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TABLE " + this.getName());
   }

   @Override
   public String toString() {
      return this.name;
   }
}
