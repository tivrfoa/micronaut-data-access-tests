package org.flywaydb.core.internal.database.postgresql;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class PostgreSQLTable extends Table<PostgreSQLDatabase, PostgreSQLSchema> {
   protected PostgreSQLTable(JdbcTemplate jdbcTemplate, PostgreSQLDatabase database, PostgreSQLSchema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TABLE " + this.database.quote(new String[]{this.schema.getName(), this.name}) + " CASCADE");
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.jdbcTemplate
         .queryForBoolean(
            "SELECT EXISTS (\n  SELECT 1\n  FROM   pg_catalog.pg_class c\n  JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace\n  WHERE  n.nspname = ?\n  AND    c.relname = ?\n  AND    c.relkind = 'r'\n)",
            this.schema.getName(),
            this.name
         );
   }

   @Override
   protected void doLock() throws SQLException {
      this.jdbcTemplate.execute("SELECT * FROM " + this + " FOR UPDATE");
   }
}
