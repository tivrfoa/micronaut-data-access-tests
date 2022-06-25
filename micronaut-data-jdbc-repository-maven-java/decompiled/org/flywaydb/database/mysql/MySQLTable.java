package org.flywaydb.database.mysql;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class MySQLTable extends Table<MySQLDatabase, MySQLSchema> {
   MySQLTable(JdbcTemplate jdbcTemplate, MySQLDatabase database, MySQLSchema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TABLE " + this.database.quote(new String[]{this.schema.getName(), this.name}));
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.exists(this.schema, null, this.name, new String[0]);
   }

   @Override
   protected void doLock() throws SQLException {
      this.jdbcTemplate.execute("SELECT * FROM " + this + " FOR UPDATE");
   }
}
