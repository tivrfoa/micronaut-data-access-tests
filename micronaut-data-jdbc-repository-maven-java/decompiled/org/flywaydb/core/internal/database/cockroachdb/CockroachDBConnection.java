package org.flywaydb.core.internal.database.cockroachdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

public class CockroachDBConnection extends Connection<CockroachDBDatabase> {
   private static final Log LOG = LogFactory.getLog(CockroachDBConnection.class);

   public CockroachDBConnection(CockroachDBDatabase database, java.sql.Connection connection) {
      super(database, connection);
   }

   @Override
   public Schema getSchema(String name) {
      return new CockroachDBSchema(this.jdbcTemplate, this.database, name);
   }

   @Override
   public Schema doGetCurrentSchema() throws SQLException {
      if (this.database.supportsSchemas()) {
         String currentSchema = this.jdbcTemplate.queryForString("SELECT current_schema");
         if (StringUtils.hasText(currentSchema)) {
            return this.getSchema(currentSchema);
         }

         String searchPath = this.getCurrentSchemaNameOrSearchPath();
         if (!StringUtils.hasText(searchPath)) {
            throw new FlywayException(
               "Unable to determine current schema as search_path is empty. Set the current schema in currentSchema parameter of the JDBC URL or in Flyway's schemas property."
            );
         }
      }

      return super.doGetCurrentSchema();
   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      if (this.database.supportsSchemas()) {
         String sp = this.jdbcTemplate.queryForString("SHOW search_path");
         if (sp.contains("$user")) {
            LOG.debug("Search path contains $user; removing...");
            ArrayList<String> paths = new ArrayList(Arrays.asList(sp.split(",")));
            paths.remove("$user");
            sp = String.join(",", paths);
         }

         return sp;
      } else {
         return this.jdbcTemplate.queryForString("SHOW database");
      }
   }

   @Override
   public void changeCurrentSchemaTo(Schema schema) {
      try {
         if (!schema.getName().equals(this.originalSchemaNameOrSearchPath) && schema.exists()) {
            this.doChangeCurrentSchemaOrSearchPathTo(schema.getName());
         }
      } catch (SQLException var3) {
         throw new FlywaySqlException("Error setting current schema to " + schema, var3);
      }
   }

   @Override
   public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
      if (this.database.supportsSchemas()) {
         if (!StringUtils.hasLength(schema)) {
            schema = "public";
         }

         this.jdbcTemplate.execute("SET search_path = " + schema);
      } else {
         if (!StringUtils.hasLength(schema)) {
            schema = "DEFAULT";
         }

         this.jdbcTemplate.execute("SET database = " + schema);
      }

   }
}
