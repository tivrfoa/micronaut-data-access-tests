package org.flywaydb.core.internal.database.hsqldb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class HSQLDBSchema extends Schema<HSQLDBDatabase, HSQLDBTable> {
   HSQLDBSchema(JdbcTemplate jdbcTemplate, HSQLDBDatabase database, String name) {
      super(jdbcTemplate, database, name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.jdbcTemplate.queryForInt("SELECT COUNT (*) FROM information_schema.system_schemas WHERE table_schem=?", this.name) > 0;
   }

   @Override
   protected boolean doEmpty() {
      return ((HSQLDBTable[])this.allTables()).length == 0;
   }

   @Override
   protected void doCreate() throws SQLException {
      String user = this.jdbcTemplate.queryForString("SELECT USER() FROM (VALUES(0))");
      this.jdbcTemplate.execute("CREATE SCHEMA " + this.database.quote(new String[]{this.name}) + " AUTHORIZATION " + user);
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP SCHEMA " + this.database.quote(new String[]{this.name}) + " CASCADE");
   }

   @Override
   protected void doClean() throws SQLException {
      for(Table table : this.allTables()) {
         table.drop();
      }

      for(String statement : this.generateDropStatementsForSequences()) {
         this.jdbcTemplate.execute(statement);
      }

   }

   private List<String> generateDropStatementsForSequences() throws SQLException {
      List<String> sequenceNames = this.jdbcTemplate
         .queryForStringList("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES where SEQUENCE_SCHEMA = ?", this.name);
      List<String> statements = new ArrayList();

      for(String seqName : sequenceNames) {
         statements.add("DROP SEQUENCE " + this.database.quote(new String[]{this.name, seqName}));
      }

      return statements;
   }

   protected HSQLDBTable[] doAllTables() throws SQLException {
      List<String> tableNames = this.jdbcTemplate
         .queryForStringList("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES where TABLE_SCHEM = ? AND TABLE_TYPE = 'TABLE'", this.name);
      HSQLDBTable[] tables = new HSQLDBTable[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new HSQLDBTable(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   @Override
   public Table getTable(String tableName) {
      return new HSQLDBTable(this.jdbcTemplate, this.database, this, tableName);
   }
}
