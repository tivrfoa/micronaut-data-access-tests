package org.flywaydb.core.internal.database.redshift;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class RedshiftSchema extends Schema<RedshiftDatabase, RedshiftTable> {
   RedshiftSchema(JdbcTemplate jdbcTemplate, RedshiftDatabase database, String name) {
      super(jdbcTemplate, database, name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM pg_namespace WHERE nspname=?", this.name) > 0;
   }

   @Override
   protected boolean doEmpty() throws SQLException {
      return !this.jdbcTemplate
         .queryForBoolean(
            "SELECT EXISTS (   SELECT 1\n   FROM   pg_catalog.pg_class c\n   JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace\n   WHERE  n.nspname = ?)",
            this.name
         );
   }

   @Override
   protected void doCreate() throws SQLException {
      this.jdbcTemplate.execute("CREATE SCHEMA " + this.database.quote(new String[]{this.name}));
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP SCHEMA " + this.database.quote(new String[]{this.name}) + " CASCADE");
   }

   @Override
   protected void doClean() throws SQLException {
      for(String statement : this.generateDropStatementsForViews()) {
         this.jdbcTemplate.execute(statement);
      }

      for(Table table : this.allTables()) {
         table.drop();
      }

      for(String statement : this.generateDropStatementsForRoutines('a', "FUNCTION", " CASCADE")) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.generateDropStatementsForRoutines('f', "FUNCTION", " CASCADE")) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.generateDropStatementsForRoutines('p', "PROCEDURE", "")) {
         this.jdbcTemplate.execute(statement);
      }

   }

   private List<String> generateDropStatementsForRoutines(char kind, String objType, String cascade) throws SQLException {
      List<Map<String, String>> rows = this.jdbcTemplate
         .queryForList(
            "SELECT proname, oidvectortypes(proargtypes) AS args FROM pg_proc_info INNER JOIN pg_namespace ns ON (pg_proc_info.pronamespace = ns.oid) LEFT JOIN pg_depend dep ON dep.objid = pg_proc_info.prooid AND dep.deptype = 'e' WHERE pg_proc_info.proisagg = false AND pg_proc_info.prokind = '"
               + kind
               + "' AND ns.nspname = ? AND dep.objid IS NULL",
            this.name
         );
      List<String> statements = new ArrayList();

      for(Map<String, String> row : rows) {
         statements.add(
            "DROP " + objType + this.database.quote(new String[]{this.name, (String)row.get("proname")}) + "(" + (String)row.get("args") + ") " + cascade
         );
      }

      return statements;
   }

   private List<String> generateDropStatementsForViews() throws SQLException {
      List<String> viewNames = this.jdbcTemplate
         .queryForStringList(
            "SELECT relname FROM pg_catalog.pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace LEFT JOIN pg_depend dep ON dep.objid = c.oid AND dep.deptype = 'e' WHERE c.relkind = 'v' AND  n.nspname = ? AND dep.objid IS NULL",
            this.name
         );
      List<String> statements = new ArrayList();

      for(String domainName : viewNames) {
         statements.add("DROP VIEW IF EXISTS " + this.database.quote(new String[]{this.name, domainName}) + " CASCADE");
      }

      return statements;
   }

   protected RedshiftTable[] doAllTables() throws SQLException {
      List<String> tableNames = this.jdbcTemplate
         .queryForStringList("SELECT t.table_name FROM information_schema.tables t WHERE table_schema=? AND table_type='BASE TABLE'", this.name);
      RedshiftTable[] tables = new RedshiftTable[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new RedshiftTable(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   @Override
   public Table getTable(String tableName) {
      return new RedshiftTable(this.jdbcTemplate, this.database, this, tableName);
   }

   @Override
   protected Type getType(String typeName) {
      return new RedshiftType(this.jdbcTemplate, this.database, this, typeName);
   }
}
