package org.flywaydb.core.internal.database.db2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.internal.database.base.Function;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class DB2Schema extends Schema<DB2Database, DB2Table> {
   DB2Schema(JdbcTemplate jdbcTemplate, DB2Database database, String name) {
      super(jdbcTemplate, database, name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.jdbcTemplate.queryForInt("SELECT count(*) from (SELECT 1 FROM syscat.schemata WHERE schemaname=?)", this.name) > 0;
   }

   @Override
   protected boolean doEmpty() throws SQLException {
      return this.jdbcTemplate
            .queryForInt(
               "select count(*) from (select 1 from syscat.tables where tabschema = ? union select 1 from syscat.views where viewschema = ? union select 1 from syscat.sequences where seqschema = ? union select 1 from syscat.indexes where indschema = ? union select 1 from syscat.routines where ROUTINESCHEMA = ? union select 1 from syscat.triggers where trigschema = ? )",
               this.name,
               this.name,
               this.name,
               this.name,
               this.name,
               this.name
            )
         == 0;
   }

   @Override
   protected void doCreate() throws SQLException {
      this.jdbcTemplate.execute("CREATE SCHEMA " + this.database.quote(new String[]{this.name}));
   }

   @Override
   protected void doDrop() throws SQLException {
      this.clean();
      this.jdbcTemplate.execute("DROP SCHEMA " + this.database.quote(new String[]{this.name}) + " RESTRICT");
   }

   @Override
   protected void doClean() throws SQLException {
      List<String> dropVersioningStatements = this.generateDropVersioningStatement();
      if (!dropVersioningStatements.isEmpty()) {
         for(String dropTableStatement : this.generateDropStatements("S", "TABLE")) {
            this.jdbcTemplate.execute(dropTableStatement);
         }
      }

      for(String dropVersioningStatement : dropVersioningStatements) {
         this.jdbcTemplate.execute(dropVersioningStatement);
      }

      for(String dropStatement : this.generateDropStatementsForViews()) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatements("A", "ALIAS")) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatements("G", "TABLE")) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(Table table : this.allTables()) {
         table.drop();
      }

      for(String dropStatement : this.generateDropStatementsForSequences()) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatementsForProcedures()) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatementsForTriggers()) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatementsForModules()) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(Function function : this.allFunctions()) {
         function.drop();
      }

      for(Type type : this.allTypes()) {
         type.drop();
      }

   }

   private List<String> generateDropStatementsForProcedures() throws SQLException {
      String dropProcGenQuery = "select SPECIFICNAME from SYSCAT.ROUTINES where ROUTINETYPE='P' and ROUTINESCHEMA = '"
         + this.name
         + "' and ROUTINEMODULENAME IS NULL";
      return this.buildDropStatements("DROP SPECIFIC PROCEDURE", dropProcGenQuery);
   }

   private List<String> generateDropStatementsForTriggers() throws SQLException {
      String dropTrigGenQuery = "select TRIGNAME from SYSCAT.TRIGGERS where TRIGSCHEMA = '" + this.name + "'";
      return this.buildDropStatements("DROP TRIGGER", dropTrigGenQuery);
   }

   private List<String> generateDropStatementsForSequences() throws SQLException {
      String dropSeqGenQuery = "select SEQNAME from SYSCAT.SEQUENCES where SEQSCHEMA = '" + this.name + "' and SEQTYPE='S'";
      return this.buildDropStatements("DROP SEQUENCE", dropSeqGenQuery);
   }

   private List<String> generateDropStatementsForViews() throws SQLException {
      String dropSeqGenQuery = "select TABNAME from SYSCAT.TABLES where TYPE='V' AND TABSCHEMA = '" + this.name + "' and substr(property,19,1) <> 'Y'";
      return this.buildDropStatements("DROP VIEW", dropSeqGenQuery);
   }

   private List<String> generateDropStatementsForModules() throws SQLException {
      String dropSeqGenQuery = "select MODULENAME from syscat.modules where MODULESCHEMA = '" + this.name + "' and OWNERTYPE='U'";
      return this.buildDropStatements("DROP MODULE", dropSeqGenQuery);
   }

   private List<String> generateDropStatements(String tableType, String objectType) throws SQLException {
      String dropTablesGenQuery = "select TABNAME from SYSCAT.TABLES where TYPE='" + tableType + "' and TABSCHEMA = '" + this.name + "'";
      return this.buildDropStatements("DROP " + objectType, dropTablesGenQuery);
   }

   private List<String> buildDropStatements(String dropPrefix, String query) throws SQLException {
      List<String> dropStatements = new ArrayList();

      for(String dbObject : this.jdbcTemplate.queryForStringList(query)) {
         dropStatements.add(dropPrefix + " " + this.database.quote(new String[]{this.name, dbObject}));
      }

      return dropStatements;
   }

   private List<String> generateDropVersioningStatement() throws SQLException {
      List<String> dropVersioningStatements = new ArrayList();
      Table[] versioningTables = this.findTables("select TABNAME from SYSCAT.TABLES where TEMPORALTYPE <> 'N' and TABSCHEMA = ?", this.name);

      for(Table table : versioningTables) {
         dropVersioningStatements.add("ALTER TABLE " + table.toString() + " DROP VERSIONING");
      }

      return dropVersioningStatements;
   }

   private DB2Table[] findTables(String sqlQuery, String... params) throws SQLException {
      List<String> tableNames = this.jdbcTemplate.queryForStringList(sqlQuery, params);
      DB2Table[] tables = new DB2Table[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new DB2Table(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   protected DB2Table[] doAllTables() throws SQLException {
      return this.findTables("select TABNAME from SYSCAT.TABLES where TYPE='T' and TABSCHEMA = ?", this.name);
   }

   @Override
   protected Function[] doAllFunctions() throws SQLException {
      List<String> functionNames = this.jdbcTemplate
         .queryForStringList(
            "select SPECIFICNAME from SYSCAT.ROUTINES where ROUTINETYPE='F' AND ORIGIN IN ('E', 'M', 'Q', 'U') and ROUTINESCHEMA = ?", this.name
         );
      List<Function> functions = new ArrayList();

      for(String functionName : functionNames) {
         functions.add(this.getFunction(functionName));
      }

      return (Function[])functions.toArray(new Function[0]);
   }

   @Override
   public Table getTable(String tableName) {
      return new DB2Table(this.jdbcTemplate, this.database, this, tableName);
   }

   @Override
   protected Type getType(String typeName) {
      return new DB2Type(this.jdbcTemplate, this.database, this, typeName);
   }

   @Override
   public Function getFunction(String functionName, String... args) {
      return new DB2Function(this.jdbcTemplate, this.database, this, functionName, args);
   }
}
