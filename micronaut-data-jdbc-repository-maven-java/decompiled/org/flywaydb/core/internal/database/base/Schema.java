package org.flywaydb.core.internal.database.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;

public abstract class Schema<D extends Database, T extends Table> {
   private static final Log LOG = LogFactory.getLog(Schema.class);
   protected final JdbcTemplate jdbcTemplate;
   protected final D database;
   protected final String name;

   public Schema(JdbcTemplate jdbcTemplate, D database, String name) {
      this.jdbcTemplate = jdbcTemplate;
      this.database = database;
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public boolean exists() {
      try {
         return this.doExists();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to check whether schema " + this + " exists", var2);
      }
   }

   protected abstract boolean doExists() throws SQLException;

   public boolean empty() {
      try {
         return this.doEmpty();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to check whether schema " + this + " is empty", var2);
      }
   }

   protected abstract boolean doEmpty() throws SQLException;

   public void create() {
      try {
         LOG.info("Creating schema " + this + " ...");
         this.doCreate();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to create schema " + this, var2);
      }
   }

   protected abstract void doCreate() throws SQLException;

   public void drop() {
      try {
         this.doDrop();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to drop schema " + this, var2);
      }
   }

   protected abstract void doDrop() throws SQLException;

   public void clean() {
      try {
         this.doClean();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to clean schema " + this, var2);
      }
   }

   protected abstract void doClean() throws SQLException;

   public T[] allTables() {
      try {
         return this.doAllTables();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to retrieve all tables in schema " + this, var2);
      }
   }

   protected abstract T[] doAllTables() throws SQLException;

   protected final Type[] allTypes() {
      ResultSet resultSet = null;

      Type[] var3;
      try {
         resultSet = this.database.jdbcMetaData.getUDTs(null, this.name, null, null);
         List<Type> types = new ArrayList();

         while(resultSet.next()) {
            types.add(this.getType(resultSet.getString("TYPE_NAME")));
         }

         var3 = (Type[])types.toArray(new Type[0]);
      } catch (SQLException var7) {
         throw new FlywaySqlException("Unable to retrieve all types in schema " + this, var7);
      } finally {
         JdbcUtils.closeResultSet(resultSet);
      }

      return var3;
   }

   protected Type getType(String typeName) {
      return null;
   }

   public abstract Table getTable(String var1);

   public Function getFunction(String functionName, String... args) {
      throw new UnsupportedOperationException("getFunction()");
   }

   protected final Function[] allFunctions() {
      try {
         return this.doAllFunctions();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to retrieve all functions in schema " + this, var2);
      }
   }

   protected Function[] doAllFunctions() throws SQLException {
      return new Function[0];
   }

   public String toString() {
      return this.database.quote(this.name);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Schema schema = (Schema)o;
         return this.name.equals(schema.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }
}
