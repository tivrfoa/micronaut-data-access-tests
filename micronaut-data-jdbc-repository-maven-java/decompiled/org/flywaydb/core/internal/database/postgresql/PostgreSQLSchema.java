package org.flywaydb.core.internal.database.postgresql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class PostgreSQLSchema extends Schema<PostgreSQLDatabase, PostgreSQLTable> {
   protected PostgreSQLSchema(JdbcTemplate jdbcTemplate, PostgreSQLDatabase database, String name) {
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
            "SELECT EXISTS (\n    SELECT c.oid FROM pg_catalog.pg_class c\n    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace\n    LEFT JOIN pg_catalog.pg_depend d ON d.objid = c.oid AND d.deptype = 'e'\n    WHERE  n.nspname = ? AND d.objid IS NULL AND c.relkind IN ('r', 'v', 'S', 't')\n  UNION ALL\n    SELECT t.oid FROM pg_catalog.pg_type t\n    JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace\n    LEFT JOIN pg_catalog.pg_depend d ON d.objid = t.oid AND d.deptype = 'e'\n    WHERE n.nspname = ? AND d.objid IS NULL AND t.typcategory NOT IN ('A', 'C')\n  UNION ALL\n    SELECT p.oid FROM pg_catalog.pg_proc p\n    JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace\n    LEFT JOIN pg_catalog.pg_depend d ON d.objid = p.oid AND d.deptype = 'e'\n    WHERE n.nspname = ? AND d.objid IS NULL\n)",
            this.name,
            this.name,
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
      for(String statement : this.generateDropStatementsForMaterializedViews()) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.generateDropStatementsForViews()) {
         this.jdbcTemplate.execute(statement);
      }

      for(Table table : this.allTables()) {
         table.drop();
      }

      for(String statement : this.generateDropStatementsForBaseTypes(true)) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.generateDropStatementsForRoutines()) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.generateDropStatementsForEnums()) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.generateDropStatementsForDomains()) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.generateDropStatementsForSequences()) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.generateDropStatementsForBaseTypes(false)) {
         this.jdbcTemplate.execute(statement);
      }

      for(String statement : this.generateDropStatementsForExtensions()) {
         this.jdbcTemplate.execute(statement);
      }

   }

   private List<String> generateDropStatementsForExtensions() throws SQLException {
      List<String> statements = new ArrayList();
      if (this.extensionsTableExists()) {
         for(String extensionName : this.jdbcTemplate
            .queryForStringList(
               "SELECT e.extname FROM pg_extension e LEFT JOIN pg_namespace n ON n.oid = e.extnamespace LEFT JOIN pg_roles r ON r.oid = e.extowner WHERE n.nspname=? AND r.rolname=?",
               this.name,
               this.database.doGetCurrentUser()
            )) {
            statements.add("DROP EXTENSION IF EXISTS " + this.database.quote(new String[]{extensionName}) + " CASCADE");
         }
      }

      return statements;
   }

   private boolean extensionsTableExists() throws SQLException {
      return this.jdbcTemplate.queryForBoolean("SELECT EXISTS ( \nSELECT 1 \nFROM pg_tables \nWHERE tablename = 'pg_extension');");
   }

   private List<String> generateDropStatementsForSequences() throws SQLException {
      List<String> sequenceNames = this.jdbcTemplate
         .queryForStringList("SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema=?", this.name);
      List<String> statements = new ArrayList();

      for(String sequenceName : sequenceNames) {
         statements.add("DROP SEQUENCE IF EXISTS " + this.database.quote(new String[]{this.name, sequenceName}));
      }

      return statements;
   }

   private List<String> generateDropStatementsForBaseTypes(boolean recreate) throws SQLException {
      List<Map<String, String>> rows = this.jdbcTemplate
         .queryForList(
            "select typname, typcategory from pg_catalog.pg_type t left join pg_depend dep on dep.objid = t.oid and dep.deptype = 'e' where (t.typrelid = 0 OR (SELECT c.relkind = 'c' FROM pg_catalog.pg_class c WHERE c.oid = t.typrelid)) and NOT EXISTS(SELECT 1 FROM pg_catalog.pg_type el WHERE el.oid = t.typelem AND el.typarray = t.oid) and t.typnamespace in (select oid from pg_catalog.pg_namespace where nspname = ?) and dep.objid is null and t.typtype != 'd'",
            this.name
         );
      List<String> statements = new ArrayList();

      for(Map<String, String> row : rows) {
         statements.add("DROP TYPE IF EXISTS " + this.database.quote(new String[]{this.name, (String)row.get("typname")}) + " CASCADE");
      }

      if (recreate) {
         for(Map<String, String> row : rows) {
            if (Arrays.asList("P", "U").contains(row.get("typcategory"))) {
               statements.add("CREATE TYPE " + this.database.quote(new String[]{this.name, (String)row.get("typname")}));
            }
         }
      }

      return statements;
   }

   private List<String> generateDropStatementsForRoutines() throws SQLException {
      String isAggregate = this.database.getVersion().isAtLeast("11") ? "pg_proc.prokind = 'a'" : "pg_proc.proisagg";
      String isProcedure = this.database.getVersion().isAtLeast("11") ? "pg_proc.prokind = 'p'" : "FALSE";
      List<Map<String, String>> rows = this.jdbcTemplate
         .queryForList(
            "SELECT proname, oidvectortypes(proargtypes) AS args, "
               + isAggregate
               + " as agg, "
               + isProcedure
               + " as proc FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) LEFT JOIN pg_depend dep ON dep.objid = pg_proc.oid AND dep.deptype = 'e' WHERE ns.nspname = ? AND dep.objid IS NULL",
            this.name
         );
      List<String> statements = new ArrayList();

      for(Map<String, String> row : rows) {
         String type = "FUNCTION";
         if (this.isTrue((String)row.get("agg"))) {
            type = "AGGREGATE";
         } else if (this.isTrue((String)row.get("proc"))) {
            type = "PROCEDURE";
         }

         statements.add(
            "DROP "
               + type
               + " IF EXISTS "
               + this.database.quote(new String[]{this.name, (String)row.get("proname")})
               + "("
               + (String)row.get("args")
               + ") CASCADE"
         );
      }

      return statements;
   }

   private boolean isTrue(String agg) {
      return agg != null && agg.toLowerCase(Locale.ENGLISH).startsWith("t");
   }

   private List<String> generateDropStatementsForEnums() throws SQLException {
      List<String> enumNames = this.jdbcTemplate
         .queryForStringList(
            "SELECT t.typname FROM pg_catalog.pg_type t INNER JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace WHERE n.nspname = ? and t.typtype = 'e'",
            this.name
         );
      List<String> statements = new ArrayList();

      for(String enumName : enumNames) {
         statements.add("DROP TYPE " + this.database.quote(new String[]{this.name, enumName}));
      }

      return statements;
   }

   private List<String> generateDropStatementsForDomains() throws SQLException {
      List<String> domainNames = this.jdbcTemplate
         .queryForStringList(
            "SELECT t.typname as domain_name\nFROM pg_catalog.pg_type t\n       LEFT JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace\n       LEFT JOIN pg_depend dep ON dep.objid = t.oid AND dep.deptype = 'e'\nWHERE t.typtype = 'd'\n  AND n.nspname = ?\n  AND dep.objid IS NULL",
            this.name
         );
      List<String> statements = new ArrayList();

      for(String domainName : domainNames) {
         statements.add("DROP DOMAIN " + this.database.quote(new String[]{this.name, domainName}));
      }

      return statements;
   }

   private List<String> generateDropStatementsForMaterializedViews() throws SQLException {
      List<String> viewNames = this.jdbcTemplate
         .queryForStringList(
            "SELECT relname FROM pg_catalog.pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relkind = 'm' AND n.nspname = ?", this.name
         );
      List<String> statements = new ArrayList();

      for(String domainName : viewNames) {
         statements.add("DROP MATERIALIZED VIEW IF EXISTS " + this.database.quote(new String[]{this.name, domainName}) + " CASCADE");
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

   protected PostgreSQLTable[] doAllTables() throws SQLException {
      List<String> tableNames = this.jdbcTemplate
         .queryForStringList(
            "SELECT t.table_name FROM information_schema.tables t LEFT JOIN pg_depend dep ON dep.objid = (quote_ident(t.table_schema)||'.'||quote_ident(t.table_name))::regclass::oid AND dep.deptype = 'e' WHERE table_schema=? AND table_type='BASE TABLE' AND dep.objid IS NULL AND NOT (SELECT EXISTS (SELECT inhrelid FROM pg_catalog.pg_inherits WHERE inhrelid = (quote_ident(t.table_schema)||'.'||quote_ident(t.table_name))::regclass::oid))",
            this.name
         );
      PostgreSQLTable[] tables = new PostgreSQLTable[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new PostgreSQLTable(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   @Override
   public Table getTable(String tableName) {
      return new PostgreSQLTable(this.jdbcTemplate, this.database, this, tableName);
   }

   @Override
   protected Type getType(String typeName) {
      return new PostgreSQLType(this.jdbcTemplate, this.database, this, typeName);
   }
}
