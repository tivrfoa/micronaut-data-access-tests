package org.flywaydb.core.internal.database.snowflake;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;

public class SnowflakeSchema extends Schema<SnowflakeDatabase, SnowflakeTable> {
   SnowflakeSchema(JdbcTemplate jdbcTemplate, SnowflakeDatabase database, String name) {
      super(jdbcTemplate, database, name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      List<Boolean> results = this.jdbcTemplate.query("SHOW SCHEMAS LIKE '" + this.name + "'", rs -> true);
      return !results.isEmpty();
   }

   @Override
   protected boolean doEmpty() throws SQLException {
      int objectCount = this.getObjectCount("TABLE") + this.getObjectCount("VIEW") + this.getObjectCount("SEQUENCE");
      return objectCount == 0;
   }

   private int getObjectCount(String objectType) throws SQLException {
      return this.jdbcTemplate.query("SHOW " + objectType + "S IN SCHEMA " + this.database.quote(new String[]{this.name}), rs -> 1).size();
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
      for(String dropStatement : this.generateDropStatements("VIEW")) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatements("TABLE")) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatements("SEQUENCE")) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatementsWithArgs("USER FUNCTIONS", "FUNCTION")) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatementsWithArgs("PROCEDURES", "PROCEDURE")) {
         this.jdbcTemplate.execute(dropStatement);
      }

   }

   protected SnowflakeTable[] doAllTables() throws SQLException {
      List<SnowflakeTable> tables = this.jdbcTemplate
         .query("SHOW TABLES IN SCHEMA " + this.database.quote(new String[]{this.name}), new RowMapper<SnowflakeTable>() {
            public SnowflakeTable mapRow(ResultSet rs) throws SQLException {
               String tableName = rs.getString("name");
               return (SnowflakeTable)SnowflakeSchema.this.getTable(tableName);
            }
         });
      return (SnowflakeTable[])tables.toArray(new SnowflakeTable[0]);
   }

   @Override
   public Table getTable(String tableName) {
      return new SnowflakeTable(this.jdbcTemplate, this.database, this, tableName);
   }

   private List<String> generateDropStatements(String objectType) throws SQLException {
      return this.jdbcTemplate.query("SHOW " + objectType + "S IN SCHEMA " + this.database.quote(new String[]{this.name}), rs -> {
         String tableName = rs.getString("name");
         return "DROP " + objectType + " " + this.database.quote(new String[]{this.name}) + "." + this.database.quote(new String[]{tableName});
      });
   }

   private List<String> generateDropStatementsWithArgs(String showObjectType, String dropObjectType) throws SQLException {
      return this.jdbcTemplate.query("SHOW " + showObjectType + " IN SCHEMA " + this.database.quote(new String[]{this.name}), rs -> {
         String nameAndArgsList = rs.getString("arguments");
         int indexOfEndOfArgs = nameAndArgsList.indexOf(") RETURN ");
         String functionName = nameAndArgsList.substring(0, indexOfEndOfArgs + 1);
         return "DROP " + dropObjectType + " " + this.name + "." + functionName;
      });
   }
}
