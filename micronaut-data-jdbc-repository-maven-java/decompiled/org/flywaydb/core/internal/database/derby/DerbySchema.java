package org.flywaydb.core.internal.database.derby;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.StringUtils;

public class DerbySchema extends Schema<DerbyDatabase, DerbyTable> {
   public DerbySchema(JdbcTemplate jdbcTemplate, DerbyDatabase database, String name) {
      super(jdbcTemplate, database, name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.jdbcTemplate.queryForInt("SELECT COUNT (*) FROM sys.sysschemas WHERE schemaname=?", this.name) > 0;
   }

   @Override
   protected boolean doEmpty() {
      return ((DerbyTable[])this.allTables()).length == 0;
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
      List<String> triggerNames = this.listObjectNames("TRIGGER", "");

      for(String statement : this.generateDropStatements("TRIGGER", triggerNames, "")) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.generateDropStatementsForConstraints()) {
         this.jdbcTemplate.execute(statement);
      }

      List<String> viewNames = this.listObjectNames("TABLE", "TABLETYPE='V'");

      for(String statement : this.generateDropStatements("VIEW", viewNames, "")) {
         this.jdbcTemplate.execute(statement);
      }

      for(Table table : this.allTables()) {
         table.drop();
      }

      List<String> sequenceNames = this.listObjectNames("SEQUENCE", "");

      for(String statement : this.generateDropStatements("SEQUENCE", sequenceNames, "RESTRICT")) {
         this.jdbcTemplate.execute(statement);
      }

   }

   private List<String> generateDropStatementsForConstraints() throws SQLException {
      List<Map<String, String>> results = this.jdbcTemplate
         .queryForList(
            "SELECT c.constraintname, t.tablename FROM sys.sysconstraints c INNER JOIN sys.systables t ON c.tableid = t.tableid INNER JOIN sys.sysschemas s ON c.schemaid = s.schemaid WHERE c.type = 'F' AND s.schemaname = ?",
            this.name
         );
      List<String> statements = new ArrayList();

      for(Map<String, String> result : results) {
         String dropStatement = "ALTER TABLE "
            + this.database.quote(new String[]{this.name, (String)result.get("TABLENAME")})
            + " DROP CONSTRAINT "
            + this.database.quote(new String[]{(String)result.get("CONSTRAINTNAME")});
         statements.add(dropStatement);
      }

      return statements;
   }

   private List<String> generateDropStatements(String objectType, List<String> objectNames, String dropStatementSuffix) {
      List<String> statements = new ArrayList();

      for(String objectName : objectNames) {
         String dropStatement = "DROP " + objectType + " " + this.database.quote(new String[]{this.name, objectName}) + " " + dropStatementSuffix;
         statements.add(dropStatement);
      }

      return statements;
   }

   protected DerbyTable[] doAllTables() throws SQLException {
      List<String> tableNames = this.listObjectNames("TABLE", "TABLETYPE='T'");
      DerbyTable[] tables = new DerbyTable[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new DerbyTable(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   private List<String> listObjectNames(String objectType, String querySuffix) throws SQLException {
      String query = "SELECT "
         + objectType
         + "name FROM sys.sys"
         + objectType
         + "s WHERE schemaid in (SELECT schemaid FROM sys.sysschemas where schemaname = ?)";
      if (StringUtils.hasLength(querySuffix)) {
         query = query + " AND " + querySuffix;
      }

      return this.jdbcTemplate.queryForStringList(query, this.name);
   }

   @Override
   public Table getTable(String tableName) {
      return new DerbyTable(this.jdbcTemplate, this.database, this, tableName);
   }
}
