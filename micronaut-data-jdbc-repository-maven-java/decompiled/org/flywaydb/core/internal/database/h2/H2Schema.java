package org.flywaydb.core.internal.database.h2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.StringUtils;

public class H2Schema extends Schema<H2Database, H2Table> {
   private static final Log LOG = LogFactory.getLog(H2Schema.class);
   private final boolean requiresV2Metadata;

   H2Schema(JdbcTemplate jdbcTemplate, H2Database database, String name, boolean requiresV2Metadata) {
      super(jdbcTemplate, database, name);
      this.requiresV2Metadata = requiresV2Metadata;
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=?", this.name) > 0;
   }

   @Override
   protected boolean doEmpty() {
      return ((H2Table[])this.allTables()).length == 0;
   }

   @Override
   protected void doCreate() throws SQLException {
      this.jdbcTemplate.execute("CREATE SCHEMA " + this.database.quote(new String[]{this.name}));
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP SCHEMA " + this.database.quote(new String[]{this.name}) + (this.database.supportsDropSchemaCascade ? " CASCADE" : ""));
   }

   @Override
   protected void doClean() throws SQLException {
      for(Table table : this.allTables()) {
         table.drop();
      }

      String sequenceSuffix = this.requiresV2Metadata ? "" : "IS_GENERATED = false";
      List<String> sequenceNames = this.listObjectNames("SEQUENCE", sequenceSuffix);

      for(String statement : this.generateDropStatements("SEQUENCE", sequenceNames)) {
         this.jdbcTemplate.execute(statement);
      }

      List<String> constantNames = this.listObjectNames("CONSTANT", "");

      for(String statement : this.generateDropStatements("CONSTANT", constantNames)) {
         this.jdbcTemplate.execute(statement);
      }

      List<String> aliasNames = this.jdbcTemplate
         .queryForStringList(
            this.requiresV2Metadata
               ? "SELECT ROUTINE_NAME FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_TYPE = 'FUNCTION' AND ROUTINE_SCHEMA = ?"
               : "SELECT ALIAS_NAME FROM INFORMATION_SCHEMA.FUNCTION_ALIASES WHERE ALIAS_SCHEMA = ?",
            this.name
         );

      for(String statement : this.generateDropStatements("ALIAS", aliasNames)) {
         this.jdbcTemplate.execute(statement);
      }

      List<String> domainNames = this.listObjectNames("DOMAIN", "");
      if (!domainNames.isEmpty()) {
         if (this.name.equals(this.database.getMainConnection().getCurrentSchema().getName())) {
            for(String statement : this.generateDropStatementsForCurrentSchema("DOMAIN", domainNames)) {
               this.jdbcTemplate.execute(statement);
            }
         } else {
            LOG.error(
               "Unable to drop DOMAIN objects in schema "
                  + this.database.quote(new String[]{this.name})
                  + " due to H2 bug! (More info: http://code.google.com/p/h2database/issues/detail?id=306)"
            );
         }
      }

   }

   private List<String> generateDropStatements(String objectType, List<String> objectNames) {
      List<String> statements = new ArrayList();

      for(String objectName : objectNames) {
         String dropStatement = "DROP " + objectType + this.database.quote(new String[]{this.name, objectName});
         statements.add(dropStatement);
      }

      return statements;
   }

   private List<String> generateDropStatementsForCurrentSchema(String objectType, List<String> objectNames) {
      List<String> statements = new ArrayList();

      for(String objectName : objectNames) {
         String dropStatement = "DROP " + objectType + this.database.quote(new String[]{objectName});
         statements.add(dropStatement);
      }

      return statements;
   }

   protected H2Table[] doAllTables() throws SQLException {
      List<String> tableNames = this.listObjectNames("TABLE", "TABLE_TYPE = " + (this.requiresV2Metadata ? "'BASE TABLE'" : "'TABLE'"));
      H2Table[] tables = new H2Table[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new H2Table(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   private List<String> listObjectNames(String objectType, String querySuffix) throws SQLException {
      String query = "SELECT " + objectType + "_NAME FROM INFORMATION_SCHEMA." + objectType + "S WHERE " + objectType + "_SCHEMA = ?";
      if (StringUtils.hasLength(querySuffix)) {
         query = query + " AND " + querySuffix;
      }

      return this.jdbcTemplate.queryForStringList(query, this.name);
   }

   @Override
   public Table getTable(String tableName) {
      return new H2Table(this.jdbcTemplate, this.database, this, tableName);
   }
}
