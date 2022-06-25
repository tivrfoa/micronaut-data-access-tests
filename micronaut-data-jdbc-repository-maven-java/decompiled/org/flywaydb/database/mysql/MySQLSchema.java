package org.flywaydb.database.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class MySQLSchema extends Schema<MySQLDatabase, MySQLTable> {
   MySQLSchema(JdbcTemplate jdbcTemplate, MySQLDatabase database, String name) {
      super(jdbcTemplate, database, name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.jdbcTemplate.queryForInt("SELECT COUNT(1) FROM information_schema.schemata WHERE schema_name=? LIMIT 1", this.name) > 0;
   }

   @Override
   protected boolean doEmpty() throws SQLException {
      List<String> params = new ArrayList(Arrays.asList(this.name, this.name, this.name, this.name, this.name));
      if (this.database.eventSchedulerQueryable) {
         params.add(this.name);
      }

      return this.jdbcTemplate
            .queryForInt(
               "SELECT SUM(found) FROM ((SELECT 1 as found FROM information_schema.tables WHERE table_schema=?) UNION ALL (SELECT 1 as found FROM information_schema.views WHERE table_schema=? LIMIT 1) UNION ALL (SELECT 1 as found FROM information_schema.table_constraints WHERE table_schema=? LIMIT 1) UNION ALL (SELECT 1 as found FROM information_schema.triggers WHERE event_object_schema=?  LIMIT 1) UNION ALL (SELECT 1 as found FROM information_schema.routines WHERE routine_schema=? LIMIT 1)"
                  + (this.database.eventSchedulerQueryable ? " UNION ALL (SELECT 1 as found FROM information_schema.events WHERE event_schema=? LIMIT 1)" : "")
                  + ") as all_found",
               (String[])params.toArray(new String[0])
            )
         == 0;
   }

   @Override
   protected void doCreate() throws SQLException {
      this.jdbcTemplate.execute("CREATE SCHEMA " + this.database.quote(new String[]{this.name}));
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP SCHEMA " + this.database.quote(new String[]{this.name}));
   }

   @Override
   protected void doClean() throws SQLException {
      if (this.database.eventSchedulerQueryable) {
         for(String statement : this.cleanEvents()) {
            this.jdbcTemplate.execute(statement);
         }
      }

      for(String statement : this.cleanRoutines()) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.cleanViews()) {
         this.jdbcTemplate.execute(statement);
      }

      this.jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

      for(Table table : this.allTables()) {
         table.drop();
      }

      this.jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

      for(String statement : this.cleanSequences()) {
         this.jdbcTemplate.execute(statement);
      }

   }

   private List<String> cleanEvents() throws SQLException {
      List<String> eventNames = this.jdbcTemplate.queryForStringList("SELECT event_name FROM information_schema.events WHERE event_schema=?", this.name);
      List<String> statements = new ArrayList();

      for(String eventName : eventNames) {
         statements.add("DROP EVENT " + this.database.quote(new String[]{this.name, eventName}));
      }

      return statements;
   }

   private List<String> cleanRoutines() throws SQLException {
      List<Map<String, String>> routineNames = this.jdbcTemplate
         .queryForList("SELECT routine_name as 'N', routine_type as 'T' FROM information_schema.routines WHERE routine_schema=?", this.name);
      List<String> statements = new ArrayList();

      for(Map<String, String> row : routineNames) {
         String routineName = (String)row.get("N");
         String routineType = (String)row.get("T");
         statements.add("DROP " + routineType + " " + this.database.quote(new String[]{this.name, routineName}));
      }

      return statements;
   }

   private List<String> cleanViews() throws SQLException {
      List<String> viewNames = this.jdbcTemplate.queryForStringList("SELECT table_name FROM information_schema.views WHERE table_schema=?", this.name);
      List<String> statements = new ArrayList();

      for(String viewName : viewNames) {
         statements.add("DROP VIEW " + this.database.quote(new String[]{this.name, viewName}));
      }

      return statements;
   }

   private List<String> cleanSequences() throws SQLException {
      List<String> names = this.jdbcTemplate
         .queryForStringList("SELECT table_name FROM information_schema.tables WHERE table_schema=? AND table_type='SEQUENCE'", this.name);
      List<String> statements = new ArrayList();

      for(String name : names) {
         statements.add("DROP SEQUENCE " + this.database.quote(new String[]{this.name, name}));
      }

      return statements;
   }

   protected MySQLTable[] doAllTables() throws SQLException {
      List<String> tableNames = this.jdbcTemplate
         .queryForStringList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema=? AND table_type IN ('BASE TABLE', 'SYSTEM VERSIONED')", this.name
         );
      MySQLTable[] tables = new MySQLTable[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new MySQLTable(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   @Override
   public Table getTable(String tableName) {
      return new MySQLTable(this.jdbcTemplate, this.database, this, tableName);
   }
}
