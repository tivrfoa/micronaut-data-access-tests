package org.flywaydb.core.internal.database.saphana;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;

public class SAPHANAConnection extends Connection<SAPHANADatabase> {
   private final boolean isCloud;

   SAPHANAConnection(SAPHANADatabase database, java.sql.Connection connection) {
      super(database, connection);

      try {
         String build = this.jdbcTemplate.queryForString("SELECT VALUE FROM M_HOST_INFORMATION WHERE KEY='build_branch'");
         this.isCloud = build.startsWith("fa/CE");
      } catch (SQLException var4) {
         throw new FlywaySqlException("Unable to determine build edition", var4);
      }
   }

   public boolean isCloudConnection() {
      return this.isCloud;
   }

   @Override
   protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
      return this.jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA FROM DUMMY");
   }

   @Override
   public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
      this.jdbcTemplate.execute("SET SCHEMA " + this.database.doQuote(schema));
   }

   @Override
   public Schema getSchema(String name) {
      return new SAPHANASchema(this.jdbcTemplate, this.database, name);
   }
}
