package org.flywaydb.core.internal.database.saphana;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class SAPHANASchema extends Schema<SAPHANADatabase, SAPHANATable> {
   SAPHANASchema(JdbcTemplate jdbcTemplate, SAPHANADatabase database, String name) {
      super(jdbcTemplate, database, name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYS.SCHEMAS WHERE SCHEMA_NAME=?", this.name) > 0;
   }

   @Override
   protected boolean doEmpty() throws SQLException {
      int objectCount = this.jdbcTemplate.queryForInt("select count(*) from sys.tables where schema_name = ?", this.name);
      objectCount += this.jdbcTemplate.queryForInt("select count(*) from sys.views where schema_name = ?", this.name);
      objectCount += this.jdbcTemplate.queryForInt("select count(*) from sys.sequences where schema_name = ?", this.name);
      objectCount += this.jdbcTemplate.queryForInt("select count(*) from sys.synonyms where schema_name = ?", this.name);
      return objectCount == 0;
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
      for(String dropStatement : this.generateDropStatements("SYNONYM")) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatementsForViews()) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatements("TABLE")) {
         this.jdbcTemplate.execute(dropStatement);
      }

      for(String dropStatement : this.generateDropStatements("SEQUENCE")) {
         this.jdbcTemplate.execute(dropStatement);
      }

   }

   private List<String> generateDropStatements(String objectType) throws SQLException {
      List<String> dropStatements = new ArrayList();

      for(String dbObject : this.getDbObjects(objectType)) {
         dropStatements.add("DROP " + objectType + " " + this.database.quote(new String[]{this.name, dbObject}) + " CASCADE");
      }

      return dropStatements;
   }

   private List<String> generateDropStatementsForViews() throws SQLException {
      List<String> dropStatements = new ArrayList();

      for(String dbObject : this.getDbObjects("VIEW")) {
         dropStatements.add("DROP VIEW " + this.database.quote(new String[]{this.name, dbObject}));
      }

      return dropStatements;
   }

   private List<String> getDbObjects(String objectType) throws SQLException {
      return this.jdbcTemplate.queryForStringList("select " + objectType + "_NAME from SYS." + objectType + "S where SCHEMA_NAME = ?", this.name);
   }

   protected SAPHANATable[] doAllTables() throws SQLException {
      List<String> tableNames = this.getDbObjects("TABLE");
      SAPHANATable[] tables = new SAPHANATable[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new SAPHANATable(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   @Override
   public Table getTable(String tableName) {
      return new SAPHANATable(this.jdbcTemplate, this.database, this, tableName);
   }
}
