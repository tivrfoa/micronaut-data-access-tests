package org.flywaydb.core.internal.database.hsqldb;

import java.sql.SQLException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class HSQLDBTable extends Table<HSQLDBDatabase, HSQLDBSchema> {
   private static final Log LOG = LogFactory.getLog(HSQLDBTable.class);

   HSQLDBTable(JdbcTemplate jdbcTemplate, HSQLDBDatabase database, HSQLDBSchema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TABLE " + this.database.quote(new String[]{this.schema.getName(), this.name}) + " CASCADE");
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.exists(null, this.schema, this.name, new String[0]);
   }

   @Override
   protected void doLock() throws SQLException {
      this.jdbcTemplate.execute("LOCK TABLE " + this + " WRITE");
   }
}
