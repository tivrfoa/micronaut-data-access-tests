package org.flywaydb.core.internal.database.snowflake;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;

public class SnowflakeTable extends Table<SnowflakeDatabase, SnowflakeSchema> {
   SnowflakeTable(JdbcTemplate jdbcTemplate, SnowflakeDatabase database, SnowflakeSchema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TABLE " + this.database.quote(new String[]{this.schema.getName()}) + "." + this.database.quote(new String[]{this.name}));
   }

   @Override
   protected boolean doExists() throws SQLException {
      if (!this.schema.exists()) {
         return false;
      } else {
         String sql = "SHOW TABLES LIKE '" + this.name + "' IN SCHEMA " + this.database.quote(new String[]{this.schema.getName()});
         List<Boolean> results = this.jdbcTemplate.query(sql, new RowMapper<Boolean>() {
            public Boolean mapRow(ResultSet rs) throws SQLException {
               return true;
            }
         });
         return !results.isEmpty();
      }
   }

   @Override
   protected void doLock() throws SQLException {
   }
}
