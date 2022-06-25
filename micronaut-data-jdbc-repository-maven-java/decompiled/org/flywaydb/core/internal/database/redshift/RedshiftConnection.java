package org.flywaydb.core.internal.database.redshift;

import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

public class RedshiftConnection extends Connection<RedshiftDatabase> {
   RedshiftConnection(RedshiftDatabase database, java.sql.Connection connection) {
      super(database, connection);
   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      return this.jdbcTemplate.queryForString("SHOW search_path");
   }

   @Override
   public void changeCurrentSchemaTo(Schema schema) {
      try {
         if (!schema.getName().equals(this.originalSchemaNameOrSearchPath)
            && !this.originalSchemaNameOrSearchPath.startsWith(schema.getName() + ",")
            && schema.exists()) {
            if (StringUtils.hasText(this.originalSchemaNameOrSearchPath) && !"unset".equals(this.originalSchemaNameOrSearchPath)) {
               this.doChangeCurrentSchemaOrSearchPathTo(schema.toString() + "," + this.originalSchemaNameOrSearchPath);
            } else {
               this.doChangeCurrentSchemaOrSearchPathTo(schema.toString());
            }

         }
      } catch (SQLException var3) {
         throw new FlywaySqlException("Error setting current schema to " + schema, var3);
      }
   }

   @Override
   public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
      if ("unset".equals(schema)) {
         schema = "";
      }

      this.jdbcTemplate.execute("SELECT set_config('search_path', ?, false)", schema);
   }

   @Override
   public Schema doGetCurrentSchema() throws SQLException {
      String currentSchema = this.jdbcTemplate.queryForString("SELECT current_schema()");
      String searchPath = this.getCurrentSchemaNameOrSearchPath();
      if (!StringUtils.hasText(currentSchema) && !StringUtils.hasText(searchPath)) {
         throw new FlywayException(
            "Unable to determine current schema as search_path is empty. Set the current schema in currentSchema parameter of the JDBC URL or in Flyway's schemas property."
         );
      } else {
         String schema = StringUtils.hasText(currentSchema) ? currentSchema : searchPath;
         return this.getSchema(schema);
      }
   }

   @Override
   public Schema getSchema(String name) {
      return new RedshiftSchema(this.jdbcTemplate, this.database, name);
   }
}
