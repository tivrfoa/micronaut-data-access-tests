package org.flywaydb.core.internal.database.informix;

import java.sql.SQLException;
import java.util.List;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class InformixSchema extends Schema<InformixDatabase, InformixTable> {
   InformixSchema(JdbcTemplate jdbcTemplate, InformixDatabase database, String name) {
      super(jdbcTemplate, database, name);
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM systables where owner = ? and tabid > 99", this.name) > 0;
   }

   @Override
   protected boolean doEmpty() throws SQLException {
      return this.doAllTables().length == 0;
   }

   @Override
   protected void doCreate() {
   }

   @Override
   protected void doDrop() throws SQLException {
      this.clean();
   }

   @Override
   protected void doClean() throws SQLException {
      for(String procedure : this.jdbcTemplate
         .queryForStringList(
            "SELECT t.procname FROM \"informix\".sysprocedures AS t WHERE t.owner=? AND t.mode='O' AND t.externalname IS NULL AND t.procname NOT IN ( 'tscontainerusage', 'tscontainertotalused', 'tscontainertotalpages', 'tscontainernelems', 'tscontainerpctused', 'tsl_flushstatus', 'tsmakenullstamp')",
            this.name
         )) {
         this.jdbcTemplate.execute("DROP PROCEDURE " + procedure);
      }

      for(Table table : this.allTables()) {
         table.drop();
      }

      for(String sequence : this.jdbcTemplate
         .queryForStringList(
            "SELECT t.tabname FROM \"informix\".systables AS t WHERE owner=? AND t.tabid > 99 AND t.tabtype='Q' AND t.tabname NOT IN ('iot_data_seq')",
            this.name
         )) {
         this.jdbcTemplate.execute("DROP SEQUENCE " + sequence);
      }

   }

   private InformixTable[] findTables(String sqlQuery, String... params) throws SQLException {
      List<String> tableNames = this.jdbcTemplate.queryForStringList(sqlQuery, params);
      InformixTable[] tables = new InformixTable[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new InformixTable(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   protected InformixTable[] doAllTables() throws SQLException {
      return this.findTables(
         "SELECT t.tabname FROM \"informix\".systables AS t WHERE owner=? AND t.tabid > 99 AND t.tabtype='T' AND t.tabname NOT IN ( 'calendarpatterns', 'calendartable', 'tscontainertable', 'tscontainerwindowtable', 'tsinstancetable',  'tscontainerusageactivewindowvti', 'tscontainerusagedormantwindowvti')",
         this.name
      );
   }

   @Override
   public Table getTable(String tableName) {
      return new InformixTable(this.jdbcTemplate, this.database, this, tableName);
   }
}
