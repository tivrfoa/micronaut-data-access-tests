package org.flywaydb.core.internal.database.sqlite;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class SQLiteSchema extends Schema<SQLiteDatabase, SQLiteTable> {
   private static final Log LOG = LogFactory.getLog(SQLiteSchema.class);
   private static final List<String> IGNORED_SYSTEM_TABLE_NAMES = Arrays.asList("android_metadata", "sqlite_sequence");
   private boolean foreignKeysEnabled;

   SQLiteSchema(JdbcTemplate jdbcTemplate, SQLiteDatabase database, String name) {
      super(jdbcTemplate, database, name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      try {
         this.doAllTables();
         return true;
      } catch (SQLException var2) {
         return false;
      }
   }

   @Override
   protected boolean doEmpty() {
      Table[] tables = this.allTables();
      List<String> tableNames = new ArrayList();

      for(Table table : tables) {
         String tableName = table.getName();
         if (!IGNORED_SYSTEM_TABLE_NAMES.contains(tableName)) {
            tableNames.add(tableName);
         }
      }

      return tableNames.isEmpty();
   }

   @Override
   protected void doCreate() {
      LOG.info("SQLite does not support creating schemas. Schema not created: " + this.name);
   }

   @Override
   protected void doDrop() {
      LOG.info("SQLite does not support dropping schemas. Schema not dropped: " + this.name);
   }

   @Override
   protected void doClean() throws SQLException {
      this.foreignKeysEnabled = this.jdbcTemplate.queryForBoolean("PRAGMA foreign_keys");

      for(String viewName : this.jdbcTemplate
         .queryForStringList("SELECT tbl_name FROM " + this.database.quote(new String[]{this.name}) + ".sqlite_master WHERE type='view'")) {
         this.jdbcTemplate.execute("DROP VIEW " + this.database.quote(new String[]{this.name, viewName}));
      }

      for(Table table : this.allTables()) {
         table.drop();
      }

      if (this.getTable("sqlite_sequence").exists()) {
         this.jdbcTemplate.execute("DELETE FROM sqlite_sequence");
      }

   }

   protected SQLiteTable[] doAllTables() throws SQLException {
      List<String> tableNames = this.jdbcTemplate
         .queryForStringList("SELECT tbl_name FROM " + this.database.quote(new String[]{this.name}) + ".sqlite_master WHERE type='table'");
      SQLiteTable[] tables = new SQLiteTable[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new SQLiteTable(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   @Override
   public Table getTable(String tableName) {
      return new SQLiteTable(this.jdbcTemplate, this.database, this, tableName);
   }

   public boolean getForeignKeysEnabled() {
      return this.foreignKeysEnabled;
   }
}
