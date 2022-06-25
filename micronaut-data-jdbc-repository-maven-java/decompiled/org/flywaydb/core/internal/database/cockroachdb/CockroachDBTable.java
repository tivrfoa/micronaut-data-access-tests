package org.flywaydb.core.internal.database.cockroachdb;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.InsertRowLock;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class CockroachDBTable extends Table<CockroachDBDatabase, CockroachDBSchema> {
   private final InsertRowLock insertRowLock;

   CockroachDBTable(JdbcTemplate jdbcTemplate, CockroachDBDatabase database, CockroachDBSchema schema, String name) {
      super(jdbcTemplate, database, schema, name);
      this.insertRowLock = new InsertRowLock(jdbcTemplate, 10);
   }

   @Override
   protected void doDrop() throws SQLException {
      new CockroachDBRetryingStrategy().execute(() -> {
         this.doDropOnce();
         return null;
      });
   }

   protected void doDropOnce() throws SQLException {
      this.jdbcTemplate.execute("DROP TABLE IF EXISTS " + this.database.quote(new String[]{this.schema.getName(), this.name}) + " CASCADE");
   }

   @Override
   protected boolean doExists() throws SQLException {
      return new CockroachDBRetryingStrategy().execute(this::doExistsOnce);
   }

   protected boolean doExistsOnce() throws SQLException {
      if (this.schema.cockroachDB1) {
         return this.jdbcTemplate
            .queryForBoolean(
               "SELECT EXISTS (\n   SELECT 1\n   FROM   information_schema.tables \n   WHERE  table_schema = ?\n   AND    table_name = ?\n)",
               this.schema.getName(),
               this.name
            );
      } else if (!this.schema.hasSchemaSupport) {
         return this.jdbcTemplate
            .queryForBoolean(
               "SELECT EXISTS (\n   SELECT 1\n   FROM   information_schema.tables \n   WHERE  table_catalog = ?\n   AND    table_schema = 'public'\n   AND    table_name = ?\n)",
               this.schema.getName(),
               this.name
            );
      } else {
         String sql = "SELECT EXISTS (\n   SELECT 1\n   FROM   information_schema.tables \n   WHERE  table_schema = ?\n   AND    table_name like '%"
            + this.name
            + "%' and length(table_name) = length(?)\n)";
         return this.jdbcTemplate.queryForBoolean(sql, this.schema.getName(), this.name);
      }
   }

   @Override
   protected void doLock() throws SQLException {
      String updateLockStatement = "UPDATE " + this + " SET installed_on = now() WHERE version = '?' AND DESCRIPTION = 'flyway-lock'";
      String deleteExpiredLockStatement = " DELETE FROM " + this + " WHERE DESCRIPTION = 'flyway-lock' AND installed_on < TIMESTAMP '?'";
      if (this.lockDepth == 0) {
         this.insertRowLock.doLock(this.database.getInsertStatement(this), updateLockStatement, deleteExpiredLockStatement, this.database.getBooleanTrue());
      }

   }

   @Override
   protected void doUnlock() throws SQLException {
      if (this.lockDepth == 1) {
         this.insertRowLock.doUnlock(this.getDeleteLockTemplate());
      }

   }

   private String getDeleteLockTemplate() {
      return "DELETE FROM " + this + " WHERE version = '?' AND DESCRIPTION = 'flyway-lock'";
   }
}
