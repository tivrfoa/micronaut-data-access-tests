package org.flywaydb.core.internal.database.base;

import java.sql.SQLException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public abstract class SchemaObject<D extends Database, S extends Schema> {
   protected final JdbcTemplate jdbcTemplate;
   protected final D database;
   protected final S schema;
   protected final String name;

   SchemaObject(JdbcTemplate jdbcTemplate, D database, S schema, String name) {
      this.name = name;
      this.jdbcTemplate = jdbcTemplate;
      this.database = database;
      this.schema = schema;
   }

   public final S getSchema() {
      return this.schema;
   }

   public final D getDatabase() {
      return this.database;
   }

   public final String getName() {
      return this.name;
   }

   public final void drop() {
      try {
         this.doDrop();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to drop " + this, var2);
      }
   }

   protected abstract void doDrop() throws SQLException;

   public String toString() {
      return this.database.quote(this.schema.getName(), this.name);
   }
}
