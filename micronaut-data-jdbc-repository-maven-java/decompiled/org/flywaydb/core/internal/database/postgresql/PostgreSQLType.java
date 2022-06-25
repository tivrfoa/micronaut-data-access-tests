package org.flywaydb.core.internal.database.postgresql;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class PostgreSQLType extends Type<PostgreSQLDatabase, PostgreSQLSchema> {
   public PostgreSQLType(JdbcTemplate jdbcTemplate, PostgreSQLDatabase database, PostgreSQLSchema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TYPE " + this.database.quote(new String[]{this.schema.getName(), this.name}));
   }
}
