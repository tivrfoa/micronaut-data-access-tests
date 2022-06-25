package org.flywaydb.core.internal.database.derby;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class DerbyTable extends Table<DerbyDatabase, DerbySchema> {
   public DerbyTable(JdbcTemplate jdbcTemplate, DerbyDatabase database, DerbySchema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TABLE " + this.database.quote(new String[]{this.schema.getName(), this.name}));
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.exists(null, this.schema, this.name, new String[0]);
   }

   @Override
   protected void doLock() throws SQLException {
      this.jdbcTemplate.execute("LOCK TABLE " + this + " IN EXCLUSIVE MODE");
   }
}
