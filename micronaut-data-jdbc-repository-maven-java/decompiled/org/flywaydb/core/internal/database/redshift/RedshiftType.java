package org.flywaydb.core.internal.database.redshift;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class RedshiftType extends Type<RedshiftDatabase, RedshiftSchema> {
   public RedshiftType(JdbcTemplate jdbcTemplate, RedshiftDatabase database, RedshiftSchema schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP TYPE " + this.database.quote(new String[]{this.schema.getName(), this.name}));
   }
}
