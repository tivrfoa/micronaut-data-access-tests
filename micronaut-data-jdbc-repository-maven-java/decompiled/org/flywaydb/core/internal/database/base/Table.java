package org.flywaydb.core.internal.database.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;

public abstract class Table<D extends Database, S extends Schema> extends SchemaObject<D, S> {
   protected int lockDepth = 0;

   public Table(JdbcTemplate jdbcTemplate, D database, S schema, String name) {
      super(jdbcTemplate, database, schema, name);
   }

   public boolean exists() {
      try {
         return this.doExists();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to check whether table " + this + " exists", var2);
      }
   }

   protected abstract boolean doExists() throws SQLException;

   protected boolean exists(Schema catalog, Schema schema, String table, String... tableTypes) throws SQLException {
      String[] types = tableTypes;
      if (tableTypes.length == 0) {
         types = null;
      }

      ResultSet resultSet = null;

      boolean found;
      try {
         resultSet = this.database.jdbcMetaData.getTables(catalog == null ? null : catalog.getName(), schema == null ? null : schema.getName(), table, types);
         found = resultSet.next();
      } finally {
         JdbcUtils.closeResultSet(resultSet);
      }

      return found;
   }

   public void lock() {
      if (this.exists()) {
         try {
            this.doLock();
            ++this.lockDepth;
         } catch (SQLException var2) {
            throw new FlywaySqlException("Unable to lock table " + this, var2);
         }
      }
   }

   protected abstract void doLock() throws SQLException;

   public void unlock() {
      if (this.exists() && this.lockDepth != 0) {
         try {
            this.doUnlock();
            --this.lockDepth;
         } catch (SQLException var2) {
            throw new FlywaySqlException("Unable to unlock table " + this, var2);
         }
      }
   }

   protected void doUnlock() throws SQLException {
   }
}
