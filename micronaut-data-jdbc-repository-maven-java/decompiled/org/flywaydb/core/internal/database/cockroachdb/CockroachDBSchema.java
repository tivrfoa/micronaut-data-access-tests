package org.flywaydb.core.internal.database.cockroachdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class CockroachDBSchema extends Schema<CockroachDBDatabase, CockroachDBTable> {
   final boolean cockroachDB1;
   final boolean hasSchemaSupport;

   public CockroachDBSchema(JdbcTemplate jdbcTemplate, CockroachDBDatabase database, String name) {
      super(jdbcTemplate, database, name);
      this.cockroachDB1 = !database.getVersion().isAtLeast("2");
      this.hasSchemaSupport = database.supportsSchemas();
   }

   @Override
   protected boolean doExists() throws SQLException {
      return new CockroachDBRetryingStrategy().execute(this::doExistsOnce);
   }

   private boolean doExistsOnce() throws SQLException {
      return this.hasSchemaSupport
         ? this.jdbcTemplate.queryForBoolean("SELECT EXISTS ( SELECT 1 FROM information_schema.schemata WHERE schema_name=? )", this.name)
         : this.jdbcTemplate.queryForBoolean("SELECT EXISTS ( SELECT 1 FROM pg_database WHERE datname=? )", this.name);
   }

   @Override
   protected boolean doEmpty() throws SQLException {
      return new CockroachDBRetryingStrategy().execute(this::doEmptyOnce);
   }

   private boolean doEmptyOnce() throws SQLException {
      if (this.cockroachDB1) {
         return !this.jdbcTemplate
            .queryForBoolean("SELECT EXISTS (  SELECT 1  FROM information_schema.tables  WHERE table_schema=?  AND table_type='BASE TABLE')", this.name);
      } else if (!this.hasSchemaSupport) {
         return !this.jdbcTemplate
            .queryForBoolean(
               "SELECT EXISTS (  SELECT 1  FROM information_schema.tables   WHERE table_catalog=?  AND table_schema='public'  AND table_type='BASE TABLE' UNION ALL  SELECT 1  FROM information_schema.sequences   WHERE sequence_catalog=?  AND sequence_schema='public')",
               this.name,
               this.name
            );
      } else {
         return !this.jdbcTemplate
            .queryForBoolean(
               "SELECT EXISTS (  SELECT 1  FROM information_schema.tables   WHERE table_schema=?  AND table_type='BASE TABLE' UNION ALL  SELECT 1  FROM information_schema.sequences   WHERE sequence_schema=?)",
               this.name,
               this.name
            );
      }
   }

   @Override
   protected void doCreate() throws SQLException {
      new CockroachDBRetryingStrategy().execute(() -> {
         this.doCreateOnce();
         return null;
      });
   }

   protected void doCreateOnce() throws SQLException {
      if (this.hasSchemaSupport) {
         this.jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + this.database.quote(new String[]{this.name}));
      } else {
         this.jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS " + this.database.quote(new String[]{this.name}));
      }

   }

   @Override
   protected void doDrop() throws SQLException {
      new CockroachDBRetryingStrategy().execute(() -> {
         this.doDropOnce();
         return null;
      });
   }

   protected void doDropOnce() throws SQLException {
      if (this.hasSchemaSupport) {
         this.jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + this.database.quote(new String[]{this.name}) + " CASCADE");
      } else {
         this.jdbcTemplate.execute("DROP DATABASE IF EXISTS " + this.database.quote(new String[]{this.name}));
      }

   }

   @Override
   protected void doClean() throws SQLException {
      new CockroachDBRetryingStrategy().execute(() -> {
         this.doCleanOnce();
         return null;
      });
   }

   protected void doCleanOnce() throws SQLException {
      for(String statement : this.generateDropStatementsForViews()) {
         this.jdbcTemplate.execute(statement);
      }

      for(Table table : this.allTables()) {
         table.drop();
      }

      for(String statement : this.generateDropStatementsForSequences()) {
         this.jdbcTemplate.execute(statement);
      }

   }

   private List<String> generateDropStatementsForViews() throws SQLException {
      List<String> names = this.hasSchemaSupport
         ? this.jdbcTemplate.queryForStringList("SELECT table_name FROM information_schema.views WHERE table_schema=?", this.name)
         : this.jdbcTemplate.queryForStringList("SELECT table_name FROM information_schema.views WHERE table_catalog=? AND table_schema='public'", this.name);
      List<String> statements = new ArrayList();

      for(String name : names) {
         statements.add("DROP VIEW IF EXISTS " + this.database.quote(new String[]{this.name, name}) + " CASCADE");
      }

      return statements;
   }

   private List<String> generateDropStatementsForSequences() throws SQLException {
      List<String> names = this.hasSchemaSupport
         ? this.jdbcTemplate.queryForStringList("SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema=?", this.name)
         : this.jdbcTemplate
            .queryForStringList("SELECT sequence_name FROM information_schema.sequences WHERE sequence_catalog=? AND sequence_schema='public'", this.name);
      List<String> statements = new ArrayList();

      for(String name : names) {
         statements.add("DROP SEQUENCE IF EXISTS " + this.database.quote(new String[]{this.name, name}) + " CASCADE");
      }

      return statements;
   }

   protected CockroachDBTable[] doAllTables() throws SQLException {
      String query;
      if (!this.cockroachDB1 && !this.hasSchemaSupport) {
         query = "SELECT table_name FROM information_schema.tables WHERE table_catalog=? AND table_schema='public' AND table_type='BASE TABLE'";
      } else {
         query = "SELECT table_name FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'";
      }

      List<String> tableNames = this.jdbcTemplate.queryForStringList(query, this.name);
      CockroachDBTable[] tables = new CockroachDBTable[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new CockroachDBTable(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   @Override
   public Table getTable(String tableName) {
      return new CockroachDBTable(this.jdbcTemplate, this.database, this, tableName);
   }
}
