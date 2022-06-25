package org.flywaydb.core.internal.database.saphana;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class SAPHANATable extends Table<SAPHANADatabase, SAPHANASchema> {
   SAPHANATable(JdbcTemplate jdbcTemplate, SAPHANADatabase database, SAPHANASchema schema, String name) {
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
      this.jdbcTemplate.update("lock table " + this + " in exclusive mode");
   }
}
