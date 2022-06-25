package org.flywaydb.core.internal.schemahistory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.RepairResult;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.jdbc.JdbcNullTypes;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

class JdbcTableSchemaHistory extends SchemaHistory {
   private static final Log LOG = LogFactory.getLog(JdbcTableSchemaHistory.class);
   private final SqlScriptExecutorFactory sqlScriptExecutorFactory;
   private final SqlScriptFactory sqlScriptFactory;
   private final Database database;
   private final Connection<?> connection;
   private final JdbcTemplate jdbcTemplate;
   private final LinkedList<AppliedMigration> cache = new LinkedList();

   JdbcTableSchemaHistory(SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScriptFactory sqlScriptFactory, Database database, Table table) {
      this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
      this.sqlScriptFactory = sqlScriptFactory;
      this.table = table;
      this.database = database;
      this.connection = database.getMainConnection();
      this.jdbcTemplate = this.connection.getJdbcTemplate();
   }

   @Override
   public void clearCache() {
      this.cache.clear();
   }

   @Override
   public boolean exists() {
      this.connection.restoreOriginalState();
      return this.table.exists();
   }

   @Override
   public void create(final boolean baseline) {
      this.connection
         .lock(
            this.table,
            new Callable<Object>() {
               public Object call() {
                  int retries = 0;
      
                  while(!JdbcTableSchemaHistory.this.exists()) {
                     if (retries == 0) {
                        JdbcTableSchemaHistory.LOG
                           .info("Creating Schema History table " + JdbcTableSchemaHistory.this.table + (baseline ? " with baseline" : "") + " ...");
                     }
      
                     try {
                        ExecutionTemplateFactory.createExecutionTemplate(
                              JdbcTableSchemaHistory.this.connection.getJdbcConnection(), JdbcTableSchemaHistory.this.database
                           )
                           .execute(
                              new Callable<Object>() {
                                 public Object call() {
                                    JdbcTableSchemaHistory.this.sqlScriptExecutorFactory
                                       .createSqlScriptExecutor(JdbcTableSchemaHistory.this.connection.getJdbcConnection(), false, false, true)
                                       .execute(
                                          JdbcTableSchemaHistory.this.database
                                             .getCreateScript(JdbcTableSchemaHistory.this.sqlScriptFactory, JdbcTableSchemaHistory.this.table, baseline)
                                       );
                                    JdbcTableSchemaHistory.LOG
                                       .debug("Created Schema History table " + JdbcTableSchemaHistory.this.table + (baseline ? " with baseline" : ""));
                                    return null;
                                 }
                              }
                           );
                     } catch (FlywayException var5) {
                        if (++retries >= 10) {
                           throw var5;
                        }
      
                        try {
                           JdbcTableSchemaHistory.LOG.debug("Schema History table creation failed. Retrying in 1 sec ...");
                           Thread.sleep(1000L);
                        } catch (InterruptedException var4) {
                        }
                     }
                  }
      
                  return null;
               }
            }
         );
   }

   @Override
   public <T> T lock(Callable<T> callable) {
      this.connection.restoreOriginalState();
      return this.connection.lock(this.table, callable);
   }

   @Override
   protected void doAddAppliedMigration(
      int installedRank, MigrationVersion version, String description, MigrationType type, String script, Integer checksum, int executionTime, boolean success
   ) {
      boolean tableIsLocked = false;
      this.connection.restoreOriginalState();
      if (!this.database.supportsDdlTransactions()) {
         this.table.lock();
         tableIsLocked = true;
      }

      try {
         String versionStr = version == null ? null : version.toString();
         if (!this.database.supportsEmptyMigrationDescription() && "".equals(description)) {
            description = "<< no description >>";
         }

         Object versionObj = versionStr == null ? JdbcNullTypes.StringNull : versionStr;
         Object checksumObj = checksum == null ? JdbcNullTypes.IntegerNull : checksum;
         this.jdbcTemplate
            .update(
               this.database.getInsertStatement(this.table),
               installedRank,
               versionObj,
               description,
               type.name(),
               script,
               checksumObj,
               this.database.getInstalledBy(),
               executionTime,
               success
            );
         LOG.debug("Schema History table " + this.table + " successfully updated to reflect changes");
      } catch (SQLException var16) {
         throw new FlywaySqlException("Unable to insert row for version '" + version + "' in Schema History table " + this.table, var16);
      } finally {
         if (tableIsLocked) {
            this.table.unlock();
         }

      }

   }

   @Override
   public List<AppliedMigration> allAppliedMigrations() {
      if (!this.exists()) {
         return new ArrayList();
      } else {
         this.refreshCache();
         return this.cache;
      }
   }

   private void refreshCache() {
      int maxCachedInstalledRank = this.cache.isEmpty() ? -1 : ((AppliedMigration)this.cache.getLast()).getInstalledRank();
      String query = this.database.getSelectStatement(this.table);

      try {
         this.cache
            .addAll(
               this.jdbcTemplate
                  .<AppliedMigration>query(
                     query,
                     new RowMapper<AppliedMigration>() {
                        public AppliedMigration mapRow(ResultSet rs) throws SQLException {
                           HashMap<String, Integer> columnOrdinalMap = JdbcTableSchemaHistory.this.constructColumnOrdinalMap(rs);
                           Integer checksum = rs.getInt(columnOrdinalMap.get("checksum"));
                           if (rs.wasNull()) {
                              checksum = null;
                           }
            
                           return new AppliedMigration(
                              rs.getInt(columnOrdinalMap.get("installed_rank")),
                              rs.getString(columnOrdinalMap.get("version")) != null
                                 ? MigrationVersion.fromVersion(rs.getString(columnOrdinalMap.get("version")))
                                 : null,
                              rs.getString(columnOrdinalMap.get("description")),
                              MigrationType.fromString(rs.getString(columnOrdinalMap.get("type"))),
                              rs.getString(columnOrdinalMap.get("script")),
                              checksum,
                              rs.getTimestamp(columnOrdinalMap.get("installed_on")),
                              rs.getString(columnOrdinalMap.get("installed_by")),
                              rs.getInt(columnOrdinalMap.get("execution_time")),
                              rs.getBoolean(columnOrdinalMap.get("success"))
                           );
                        }
                     },
                     maxCachedInstalledRank
                  )
            );
      } catch (SQLException var4) {
         throw new FlywaySqlException("Error while retrieving the list of applied migrations from Schema History table " + this.table, var4);
      }
   }

   private HashMap<String, Integer> constructColumnOrdinalMap(ResultSet rs) throws SQLException {
      HashMap<String, Integer> columnOrdinalMap = new HashMap();
      ResultSetMetaData metadata = rs.getMetaData();

      for(int i = 1; i <= metadata.getColumnCount(); ++i) {
         String columnNameLower = metadata.getColumnName(i).toLowerCase();
         columnOrdinalMap.put(columnNameLower, i);
      }

      return columnOrdinalMap;
   }

   @Override
   public boolean removeFailedMigrations(RepairResult repairResult, MigrationPattern[] migrationPatternFilter) {
      if (!this.exists()) {
         LOG.info("Repair of failed migration in Schema History table " + this.table + " not necessary as table doesn't exist.");
         return false;
      } else {
         List<AppliedMigration> appliedMigrations = this.filterMigrations(this.allAppliedMigrations(), migrationPatternFilter);
         boolean failed = appliedMigrations.stream().anyMatch(am -> !am.isSuccess());
         if (!failed) {
            LOG.info("Repair of failed migration in Schema History table " + this.table + " not necessary. No failed migration detected.");
            return false;
         } else {
            try {
               appliedMigrations.stream()
                  .filter(am -> !am.isSuccess())
                  .forEach(am -> repairResult.migrationsRemoved.add(CommandResultFactory.createRepairOutput(am)));

               for(AppliedMigration appliedMigration : appliedMigrations) {
                  this.jdbcTemplate
                     .execute(
                        "DELETE FROM "
                           + this.table
                           + " WHERE "
                           + this.database.quote("success")
                           + " = "
                           + this.database.getBooleanFalse()
                           + " AND "
                           + (
                              appliedMigration.getVersion() != null
                                 ? this.database.quote("version") + " = '" + appliedMigration.getVersion().getVersion() + "'"
                                 : this.database.quote("description") + " = '" + appliedMigration.getDescription() + "'"
                           )
                     );
               }

               this.clearCache();
               return true;
            } catch (SQLException var7) {
               throw new FlywaySqlException("Unable to repair Schema History table " + this.table, var7);
            }
         }
      }
   }

   private List<AppliedMigration> filterMigrations(List<AppliedMigration> appliedMigrations, MigrationPattern[] migrationPatternFilter) {
      if (migrationPatternFilter == null) {
         return appliedMigrations;
      } else {
         Set<AppliedMigration> filteredList = new HashSet();

         for(AppliedMigration appliedMigration : appliedMigrations) {
            for(MigrationPattern migrationPattern : migrationPatternFilter) {
               if (migrationPattern.matches(appliedMigration.getVersion(), appliedMigration.getDescription())) {
                  filteredList.add(appliedMigration);
               }
            }
         }

         return new ArrayList(filteredList);
      }
   }

   @Override
   public void update(AppliedMigration appliedMigration, ResolvedMigration resolvedMigration) {
      this.connection.restoreOriginalState();
      this.clearCache();
      MigrationVersion version = appliedMigration.getVersion();
      String description = resolvedMigration.getDescription();
      Integer checksum = resolvedMigration.getChecksum();
      MigrationType type = appliedMigration.getType().isSynthetic() ? appliedMigration.getType() : resolvedMigration.getType();
      LOG.info(
         "Repairing Schema History table for version " + version + " (Description: " + description + ", Type: " + type + ", Checksum: " + checksum + ")  ..."
      );
      if (!this.database.supportsEmptyMigrationDescription() && "".equals(description)) {
         description = "<< no description >>";
      }

      Object checksumObj = checksum == null ? JdbcNullTypes.IntegerNull : checksum;

      try {
         this.jdbcTemplate
            .update(
               "UPDATE "
                  + this.table
                  + " SET "
                  + this.database.quote("description")
                  + "=? , "
                  + this.database.quote("type")
                  + "=? , "
                  + this.database.quote("checksum")
                  + "=? WHERE "
                  + this.database.quote("installed_rank")
                  + "=?",
               description,
               type.name(),
               checksumObj,
               appliedMigration.getInstalledRank()
            );
      } catch (SQLException var9) {
         throw new FlywaySqlException("Unable to repair Schema History table " + this.table + " for version " + version, var9);
      }
   }

   @Override
   public void delete(AppliedMigration appliedMigration) {
      this.connection.restoreOriginalState();
      this.clearCache();
      MigrationVersion version = appliedMigration.getVersion();
      String versionStr = version == null ? null : version.toString();
      if (version == null) {
         LOG.info("Repairing Schema History table for description \"" + appliedMigration.getDescription() + "\" (Marking as DELETED)  ...");
      } else {
         LOG.info("Repairing Schema History table for version \"" + version + "\" (Marking as DELETED)  ...");
      }

      Object versionObj = versionStr == null ? JdbcNullTypes.StringNull : versionStr;
      Object checksumObj = appliedMigration.getChecksum() == null ? JdbcNullTypes.IntegerNull : appliedMigration.getChecksum();

      try {
         this.jdbcTemplate
            .update(
               this.database.getInsertStatement(this.table),
               this.calculateInstalledRank(),
               versionObj,
               appliedMigration.getDescription(),
               "DELETE",
               appliedMigration.getScript(),
               checksumObj,
               this.database.getInstalledBy(),
               0,
               appliedMigration.isSuccess()
            );
      } catch (SQLException var7) {
         throw new FlywaySqlException("Unable to repair Schema History table " + this.table + " for version " + version, var7);
      }
   }
}
