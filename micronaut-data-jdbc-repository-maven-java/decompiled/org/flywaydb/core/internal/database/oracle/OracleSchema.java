package org.flywaydb.core.internal.database.oracle;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.StringUtils;

public class OracleSchema extends Schema<OracleDatabase, OracleTable> {
   private static final Log LOG = LogFactory.getLog(OracleSchema.class);

   OracleSchema(JdbcTemplate jdbcTemplate, OracleDatabase database, String name) {
      super(jdbcTemplate, database, name);
   }

   public boolean isSystem() throws SQLException {
      return this.database.getSystemSchemas().contains(this.name);
   }

   boolean isDefaultSchemaForUser() throws SQLException {
      return this.name.equals(this.database.doGetCurrentUser());
   }

   @Override
   protected boolean doExists() throws SQLException {
      return this.database.queryReturnsRows("SELECT * FROM ALL_USERS WHERE USERNAME = ?", this.name);
   }

   @Override
   protected boolean doEmpty() throws SQLException {
      return !OracleSchema.ObjectType.supportedTypesExist(this.jdbcTemplate, this.database, this);
   }

   @Override
   protected void doCreate() throws SQLException {
      this.jdbcTemplate
         .execute("CREATE USER " + this.database.quote(new String[]{this.name}) + " IDENTIFIED BY " + this.database.quote(new String[]{"FFllyywwaayy00!!"}));
      this.jdbcTemplate.execute("GRANT RESOURCE TO " + this.database.quote(new String[]{this.name}));
      this.jdbcTemplate.execute("GRANT UNLIMITED TABLESPACE TO " + this.database.quote(new String[]{this.name}));
   }

   @Override
   protected void doDrop() throws SQLException {
      this.jdbcTemplate.execute("DROP USER " + this.database.quote(new String[]{this.name}) + " CASCADE");
   }

   @Override
   protected void doClean() throws SQLException {
      if (this.isSystem()) {
         throw new FlywayException(
            "Clean not supported on Oracle for system schema "
               + this.database.quote(new String[]{this.name})
               + "! It must not be changed in any way except by running an Oracle-supplied script!"
         );
      } else {
         if (this.database.isFlashbackDataArchiveAvailable()) {
            this.disableFlashbackArchiveForFbaTrackedTables();
         }

         if (this.database.isLocatorAvailable()) {
            this.cleanLocatorMetadata();
         }

         Set<String> objectTypeNames = OracleSchema.ObjectType.getObjectTypeNames(this.jdbcTemplate, this.database, this);

         for(OracleSchema.ObjectType objectType : Arrays.asList(
            OracleSchema.ObjectType.TRIGGER,
            OracleSchema.ObjectType.QUEUE_TABLE,
            OracleSchema.ObjectType.FILE_WATCHER,
            OracleSchema.ObjectType.SCHEDULER_CHAIN,
            OracleSchema.ObjectType.SCHEDULER_JOB,
            OracleSchema.ObjectType.SCHEDULER_PROGRAM,
            OracleSchema.ObjectType.SCHEDULE,
            OracleSchema.ObjectType.RULE_SET,
            OracleSchema.ObjectType.RULE,
            OracleSchema.ObjectType.EVALUATION_CONTEXT,
            OracleSchema.ObjectType.FILE_GROUP,
            OracleSchema.ObjectType.XML_SCHEMA,
            OracleSchema.ObjectType.MINING_MODEL,
            OracleSchema.ObjectType.REWRITE_EQUIVALENCE,
            OracleSchema.ObjectType.SQL_TRANSLATION_PROFILE,
            OracleSchema.ObjectType.MATERIALIZED_VIEW,
            OracleSchema.ObjectType.MATERIALIZED_VIEW_LOG,
            OracleSchema.ObjectType.DIMENSION,
            OracleSchema.ObjectType.VIEW,
            OracleSchema.ObjectType.DOMAIN_INDEX,
            OracleSchema.ObjectType.DOMAIN_INDEX_TYPE,
            OracleSchema.ObjectType.TABLE,
            OracleSchema.ObjectType.INDEX,
            OracleSchema.ObjectType.CLUSTER,
            OracleSchema.ObjectType.SEQUENCE,
            OracleSchema.ObjectType.OPERATOR,
            OracleSchema.ObjectType.FUNCTION,
            OracleSchema.ObjectType.PROCEDURE,
            OracleSchema.ObjectType.PACKAGE,
            OracleSchema.ObjectType.CONTEXT,
            OracleSchema.ObjectType.LIBRARY,
            OracleSchema.ObjectType.TYPE,
            OracleSchema.ObjectType.SYNONYM,
            OracleSchema.ObjectType.JAVA_SOURCE,
            OracleSchema.ObjectType.JAVA_CLASS,
            OracleSchema.ObjectType.JAVA_RESOURCE,
            OracleSchema.ObjectType.DATABASE_LINK,
            OracleSchema.ObjectType.CREDENTIAL,
            OracleSchema.ObjectType.DATABASE_DESTINATION,
            OracleSchema.ObjectType.SCHEDULER_GROUP,
            OracleSchema.ObjectType.CUBE,
            OracleSchema.ObjectType.CUBE_DIMENSION,
            OracleSchema.ObjectType.CUBE_BUILD_PROCESS,
            OracleSchema.ObjectType.MEASURE_FOLDER,
            OracleSchema.ObjectType.ASSEMBLY,
            OracleSchema.ObjectType.JAVA_DATA
         )) {
            if (objectTypeNames.contains(objectType.getName())) {
               LOG.debug("Cleaning objects of type " + objectType + " ...");
               objectType.dropObjects(this.jdbcTemplate, this.database, this);
            }
         }

         if (this.isDefaultSchemaForUser()) {
            this.jdbcTemplate.execute("PURGE RECYCLEBIN");
         }

      }
   }

   private void disableFlashbackArchiveForFbaTrackedTables() throws SQLException {
      boolean dbaViewAccessible = this.database.isPrivOrRoleGranted("SELECT ANY DICTIONARY")
         || this.database.isDataDictViewAccessible("DBA_FLASHBACK_ARCHIVE_TABLES");
      if (!dbaViewAccessible && !this.isDefaultSchemaForUser()) {
         LOG.warn(
            "Unable to check and disable Flashback Archive for tables in schema "
               + this.database.quote(new String[]{this.name})
               + " by user \""
               + this.database.doGetCurrentUser()
               + "\": DBA_FLASHBACK_ARCHIVE_TABLES is not accessible"
         );
      } else {
         boolean oracle18orNewer = this.database.getVersion().isAtLeast("18");
         String queryForFbaTrackedTables = "SELECT TABLE_NAME FROM "
            + (dbaViewAccessible ? "DBA_" : "USER_")
            + "FLASHBACK_ARCHIVE_TABLES WHERE OWNER_NAME = ?"
            + (oracle18orNewer ? " AND STATUS='ENABLED'" : "");

         for(String tableName : this.jdbcTemplate.queryForStringList(queryForFbaTrackedTables, this.name)) {
            this.jdbcTemplate.execute("ALTER TABLE " + this.database.quote(new String[]{this.name, tableName}) + " NO FLASHBACK ARCHIVE");

            while(this.database.queryReturnsRows(queryForFbaTrackedTables + " AND TABLE_NAME = ?", this.name, tableName)) {
               try {
                  LOG.debug("Actively waiting for Flashback cleanup on table: " + this.database.quote(new String[]{this.name, tableName}));
                  Thread.sleep(1000L);
               } catch (InterruptedException var9) {
                  throw new FlywayException("Waiting for Flashback cleanup interrupted", var9);
               }
            }
         }

         if (oracle18orNewer) {
            while(this.database.queryReturnsRows("SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ?\n AND TABLE_NAME LIKE 'SYS_FBA_DDL_COLMAP_%'", this.name)) {
               try {
                  LOG.debug("Actively waiting for Flashback colmap cleanup");
                  Thread.sleep(1000L);
               } catch (InterruptedException var8) {
                  throw new FlywayException("Waiting for Flashback colmap cleanup interrupted", var8);
               }
            }
         }

      }
   }

   private boolean locatorMetadataExists() throws SQLException {
      return this.database.queryReturnsRows("SELECT * FROM ALL_SDO_GEOM_METADATA WHERE OWNER = ?", this.name);
   }

   private void cleanLocatorMetadata() throws SQLException {
      if (this.locatorMetadataExists()) {
         if (!this.isDefaultSchemaForUser()) {
            LOG.warn(
               "Unable to clean Oracle Locator metadata for schema "
                  + this.database.quote(new String[]{this.name})
                  + " by user \""
                  + this.database.doGetCurrentUser()
                  + "\": unsupported operation"
            );
         } else {
            this.jdbcTemplate.getConnection().commit();
            this.jdbcTemplate.execute("DELETE FROM USER_SDO_GEOM_METADATA");
            this.jdbcTemplate.getConnection().commit();
         }
      }
   }

   protected OracleTable[] doAllTables() throws SQLException {
      List<String> tableNames = OracleSchema.ObjectType.TABLE.getObjectNames(this.jdbcTemplate, this.database, this);
      OracleTable[] tables = new OracleTable[tableNames.size()];

      for(int i = 0; i < tableNames.size(); ++i) {
         tables[i] = new OracleTable(this.jdbcTemplate, this.database, this, (String)tableNames.get(i));
      }

      return tables;
   }

   @Override
   public Table getTable(String tableName) {
      return new OracleTable(this.jdbcTemplate, this.database, this, tableName);
   }

   public static enum ObjectType {
      TABLE("TABLE", "CASCADE CONSTRAINTS PURGE") {
         @Override
         public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            boolean referencePartitionedTablesExist = database.queryReturnsRows(
               "SELECT * FROM ALL_PART_TABLES WHERE OWNER = ? AND PARTITIONING_TYPE = 'REFERENCE'", schema.getName()
            );
            boolean xmlDbAvailable = database.isXmlDbAvailable();
            StringBuilder tablesQuery = new StringBuilder();
            tablesQuery.append(
               "WITH TABLES AS (\n  SELECT TABLE_NAME, OWNER\n  FROM ALL_TABLES\n  WHERE OWNER = ?\n    AND (IOT_TYPE IS NULL OR IOT_TYPE NOT LIKE '%OVERFLOW%')\n    AND NESTED != 'YES'\n    AND SECONDARY != 'Y'\n"
            );
            if (xmlDbAvailable) {
               tablesQuery.append(
                  "  UNION ALL\n  SELECT TABLE_NAME, OWNER\n  FROM ALL_XML_TABLES\n  WHERE OWNER = ?\n    AND TABLE_NAME NOT LIKE 'BIN$________________________$_'\n"
               );
            }

            tablesQuery.append(")\nSELECT t.TABLE_NAME\nFROM TABLES t\n");
            if (referencePartitionedTablesExist) {
               tablesQuery.append(
                  "  LEFT JOIN ALL_PART_TABLES pt\n    ON t.OWNER = pt.OWNER\n   AND t.TABLE_NAME = pt.TABLE_NAME\n   AND pt.PARTITIONING_TYPE = 'REFERENCE'\n  LEFT JOIN ALL_CONSTRAINTS fk\n    ON pt.OWNER = fk.OWNER\n   AND pt.TABLE_NAME = fk.TABLE_NAME\n   AND pt.REF_PTN_CONSTRAINT_NAME = fk.CONSTRAINT_NAME\n   AND fk.CONSTRAINT_TYPE = 'R'\n  LEFT JOIN ALL_CONSTRAINTS puk\n    ON fk.R_OWNER = puk.OWNER\n   AND fk.R_CONSTRAINT_NAME = puk.CONSTRAINT_NAME\n   AND puk.CONSTRAINT_TYPE IN ('P', 'U')\n  LEFT JOIN TABLES p\n    ON puk.OWNER = p.OWNER\n   AND puk.TABLE_NAME = p.TABLE_NAME\nSTART WITH p.TABLE_NAME IS NULL\nCONNECT BY PRIOR t.TABLE_NAME = p.TABLE_NAME\nORDER BY LEVEL DESC"
               );
            }

            int n = 1 + (xmlDbAvailable ? 1 : 0);
            String[] params = new String[n];
            Arrays.fill(params, schema.getName());
            return jdbcTemplate.queryForStringList(tablesQuery.toString(), params);
         }
      },
      QUEUE_TABLE("QUEUE TABLE") {
         @Override
         public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return jdbcTemplate.queryForStringList("SELECT QUEUE_TABLE FROM ALL_QUEUE_TABLES WHERE OWNER = ?", schema.getName());
         }

         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_AQADM.DROP_QUEUE_TABLE('" + database.quote(new String[]{schema.getName(), objectName}) + "', FORCE => TRUE); END;";
         }
      },
      MATERIALIZED_VIEW_LOG("MATERIALIZED VIEW LOG") {
         @Override
         public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return jdbcTemplate.queryForStringList("SELECT MASTER FROM ALL_MVIEW_LOGS WHERE LOG_OWNER = ?", schema.getName());
         }

         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "DROP " + this.getName() + " ON " + database.quote(new String[]{schema.getName(), objectName});
         }
      },
      INDEX("INDEX") {
         @Override
         public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return jdbcTemplate.queryForStringList("SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND INDEX_TYPE NOT LIKE '%DOMAIN%'", schema.getName());
         }
      },
      DOMAIN_INDEX("INDEX", "FORCE") {
         @Override
         public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return jdbcTemplate.queryForStringList("SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND INDEX_TYPE LIKE '%DOMAIN%'", schema.getName());
         }
      },
      DOMAIN_INDEX_TYPE("INDEXTYPE", "FORCE"),
      OPERATOR("OPERATOR", "FORCE"),
      CLUSTER("CLUSTER", "INCLUDING TABLES CASCADE CONSTRAINTS"),
      VIEW("VIEW", "CASCADE CONSTRAINTS"),
      MATERIALIZED_VIEW("MATERIALIZED VIEW", "PRESERVE TABLE"),
      DIMENSION("DIMENSION") {
         @Override
         public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return jdbcTemplate.queryForStringList("SELECT DIMENSION_NAME FROM ALL_DIMENSIONS WHERE OWNER = ?", schema.getName());
         }
      },
      SYNONYM("SYNONYM", "FORCE"),
      SEQUENCE("SEQUENCE"),
      PROCEDURE("PROCEDURE"),
      FUNCTION("FUNCTION"),
      PACKAGE("PACKAGE"),
      CONTEXT("CONTEXT") {
         @Override
         public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return jdbcTemplate.queryForStringList("SELECT NAMESPACE FROM " + database.dbaOrAll("CONTEXT") + " WHERE SCHEMA = ?", schema.getName());
         }

         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "DROP " + this.getName() + " " + database.quote(new String[]{objectName});
         }
      },
      TRIGGER("TRIGGER"),
      TYPE("TYPE", "FORCE"),
      JAVA_SOURCE("JAVA SOURCE"),
      JAVA_CLASS("JAVA CLASS"),
      JAVA_RESOURCE("JAVA RESOURCE"),
      LIBRARY("LIBRARY"),
      XML_SCHEMA("XML SCHEMA") {
         @Override
         public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return !database.isXmlDbAvailable()
               ? Collections.emptyList()
               : jdbcTemplate.queryForStringList("SELECT QUAL_SCHEMA_URL FROM " + database.dbaOrAll("XML_SCHEMAS") + " WHERE OWNER = ?", schema.getName());
         }

         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_XMLSCHEMA.DELETESCHEMA('" + objectName + "', DELETE_OPTION => DBMS_XMLSCHEMA.DELETE_CASCADE_FORCE); END;";
         }
      },
      REWRITE_EQUIVALENCE("REWRITE EQUIVALENCE") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN SYS.DBMS_ADVANCED_REWRITE.DROP_REWRITE_EQUIVALENCE('" + database.quote(new String[]{schema.getName(), objectName}) + "'); END;";
         }
      },
      SQL_TRANSLATION_PROFILE("SQL TRANSLATION PROFILE") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_SQL_TRANSLATOR.DROP_PROFILE('" + database.quote(new String[]{schema.getName(), objectName}) + "'); END;";
         }
      },
      MINING_MODEL("MINING MODEL") {
         @Override
         public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return super.getObjectNames(jdbcTemplate, database, schema);
         }

         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_DATA_MINING.DROP_MODEL('" + database.quote(new String[]{schema.getName(), objectName}) + "'); END;";
         }
      },
      SCHEDULER_JOB("JOB") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_SCHEDULER.DROP_JOB('" + database.quote(new String[]{schema.getName(), objectName}) + "', FORCE => TRUE); END;";
         }
      },
      SCHEDULER_PROGRAM("PROGRAM") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_SCHEDULER.DROP_PROGRAM('" + database.quote(new String[]{schema.getName(), objectName}) + "', FORCE => TRUE); END;";
         }
      },
      SCHEDULE("SCHEDULE") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_SCHEDULER.DROP_SCHEDULE('" + database.quote(new String[]{schema.getName(), objectName}) + "', FORCE => TRUE); END;";
         }
      },
      SCHEDULER_CHAIN("CHAIN") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_SCHEDULER.DROP_CHAIN('" + database.quote(new String[]{schema.getName(), objectName}) + "', FORCE => TRUE); END;";
         }
      },
      FILE_WATCHER("FILE WATCHER") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_SCHEDULER.DROP_FILE_WATCHER('" + database.quote(new String[]{schema.getName(), objectName}) + "', FORCE => TRUE); END;";
         }
      },
      RULE_SET("RULE SET") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_RULE_ADM.DROP_RULE_SET('" + database.quote(new String[]{schema.getName(), objectName}) + "', DELETE_RULES => FALSE); END;";
         }
      },
      RULE("RULE") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_RULE_ADM.DROP_RULE('" + database.quote(new String[]{schema.getName(), objectName}) + "', FORCE => TRUE); END;";
         }
      },
      EVALUATION_CONTEXT("EVALUATION CONTEXT") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_RULE_ADM.DROP_EVALUATION_CONTEXT('" + database.quote(new String[]{schema.getName(), objectName}) + "', FORCE => TRUE); END;";
         }
      },
      FILE_GROUP("FILE GROUP") {
         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_FILE_GROUP.DROP_FILE_GROUP('" + database.quote(new String[]{schema.getName(), objectName}) + "'); END;";
         }
      },
      DATABASE_LINK("DATABASE LINK") {
         @Override
         public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
            super.warnUnsupported(database.quote(new String[]{schema.getName()}));
         }

         @Override
         public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return jdbcTemplate.queryForStringList("SELECT DB_LINK FROM " + database.dbaOrAll("DB_LINKS") + " WHERE OWNER = ?", schema.getName());
         }

         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "DROP " + this.getName() + " " + objectName;
         }
      },
      CREDENTIAL("CREDENTIAL") {
         @Override
         public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
            super.warnUnsupported(database.quote(new String[]{schema.getName()}));
         }

         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_SCHEDULER.DROP_CREDENTIAL('" + database.quote(new String[]{schema.getName(), objectName}) + "', FORCE => TRUE); END;";
         }
      },
      DATABASE_DESTINATION("DESTINATION") {
         @Override
         public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
            super.warnUnsupported(database.quote(new String[]{schema.getName()}));
         }

         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_SCHEDULER.DROP_DATABASE_DESTINATION('" + database.quote(new String[]{schema.getName(), objectName}) + "'); END;";
         }
      },
      SCHEDULER_GROUP("SCHEDULER GROUP") {
         @Override
         public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
            super.warnUnsupported(database.quote(new String[]{schema.getName()}));
         }

         @Override
         public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "BEGIN DBMS_SCHEDULER.DROP_GROUP('" + database.quote(new String[]{schema.getName(), objectName}) + "', FORCE => TRUE); END;";
         }
      },
      CUBE("CUBE") {
         @Override
         public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
            super.warnUnsupported(database.quote(new String[]{schema.getName()}));
         }
      },
      CUBE_DIMENSION("CUBE DIMENSION") {
         @Override
         public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
            super.warnUnsupported(database.quote(new String[]{schema.getName()}));
         }
      },
      CUBE_BUILD_PROCESS("CUBE BUILD PROCESS") {
         @Override
         public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
            super.warnUnsupported(database.quote(new String[]{schema.getName()}), "cube build processes");
         }
      },
      MEASURE_FOLDER("MEASURE FOLDER") {
         @Override
         public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
            super.warnUnsupported(database.quote(new String[]{schema.getName()}));
         }
      },
      ASSEMBLY("ASSEMBLY") {
         @Override
         public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
            super.warnUnsupported(database.quote(new String[]{schema.getName()}), "assemblies");
         }
      },
      JAVA_DATA("JAVA DATA") {
         @Override
         public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
            super.warnUnsupported(database.quote(new String[]{schema.getName()}));
         }
      },
      CAPTURE("CAPTURE"),
      APPLY("APPLY"),
      DIRECTORY("DIRECTORY"),
      RESOURCE_PLAN("RESOURCE PLAN"),
      CONSUMER_GROUP("CONSUMER GROUP"),
      JOB_CLASS("JOB CLASS"),
      WINDOWS("WINDOW"),
      EDITION("EDITION"),
      AGENT_DESTINATION("DESTINATION"),
      UNIFIED_AUDIT_POLICY("UNIFIED AUDIT POLICY");

      private final String name;
      private final String dropOptions;

      private ObjectType(String name, String dropOptions) {
         this.name = name;
         this.dropOptions = dropOptions;
      }

      private ObjectType(String name) {
         this(name, "");
      }

      public String getName() {
         return this.name;
      }

      public String toString() {
         return super.toString().replace('_', ' ');
      }

      public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
         return jdbcTemplate.queryForStringList(
            "SELECT DISTINCT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_TYPE = ?", schema.getName(), this.getName()
         );
      }

      public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
         return "DROP "
            + this.getName()
            + " "
            + database.quote(new String[]{schema.getName(), objectName})
            + (StringUtils.hasText(this.dropOptions) ? " " + this.dropOptions : "");
      }

      public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
         for(String objectName : this.getObjectNames(jdbcTemplate, database, schema)) {
            jdbcTemplate.execute(this.generateDropStatement(jdbcTemplate, database, schema, objectName));
         }

      }

      private void warnUnsupported(String schemaName, String typeDesc) {
         OracleSchema.LOG.warn("Unable to clean " + typeDesc + " for schema " + schemaName + ": unsupported operation");
      }

      private void warnUnsupported(String schemaName) {
         this.warnUnsupported(schemaName, this.toString().toLowerCase() + "s");
      }

      public static Set<String> getObjectTypeNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
         boolean xmlDbAvailable = database.isXmlDbAvailable();
         String query = "SELECT DISTINCT OBJECT_TYPE FROM "
            + database.dbaOrAll("OBJECTS")
            + " WHERE OWNER = ? UNION SELECT '"
            + MATERIALIZED_VIEW_LOG.getName()
            + "' FROM DUAL WHERE EXISTS(SELECT * FROM ALL_MVIEW_LOGS WHERE LOG_OWNER = ?) UNION SELECT '"
            + DIMENSION.getName()
            + "' FROM DUAL WHERE EXISTS(SELECT * FROM ALL_DIMENSIONS WHERE OWNER = ?) UNION SELECT '"
            + QUEUE_TABLE.getName()
            + "' FROM DUAL WHERE EXISTS(SELECT * FROM ALL_QUEUE_TABLES WHERE OWNER = ?) UNION SELECT '"
            + DATABASE_LINK.getName()
            + "' FROM DUAL WHERE EXISTS(SELECT * FROM "
            + database.dbaOrAll("DB_LINKS")
            + " WHERE OWNER = ?) UNION SELECT '"
            + CONTEXT.getName()
            + "' FROM DUAL WHERE EXISTS(SELECT * FROM "
            + database.dbaOrAll("CONTEXT")
            + " WHERE SCHEMA = ?) "
            + (
               xmlDbAvailable
                  ? "UNION SELECT '"
                     + XML_SCHEMA.getName()
                     + "' FROM DUAL WHERE EXISTS(SELECT * FROM "
                     + database.dbaOrAll("XML_SCHEMAS")
                     + " WHERE OWNER = ?) "
                  : ""
            )
            + "UNION SELECT '"
            + CREDENTIAL.getName()
            + "' FROM DUAL WHERE EXISTS(SELECT * FROM ALL_SCHEDULER_CREDENTIALS WHERE OWNER = ?) ";
         int n = 6 + (xmlDbAvailable ? 1 : 0) + 1;
         String[] params = new String[n];
         Arrays.fill(params, schema.getName());
         return new HashSet(jdbcTemplate.queryForStringList(query, params));
      }

      public static boolean supportedTypesExist(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
         Set<String> existingTypeNames = new HashSet(getObjectTypeNames(jdbcTemplate, database, schema));
         existingTypeNames.removeAll(
            Arrays.asList(
               DATABASE_LINK.getName(),
               CREDENTIAL.getName(),
               DATABASE_DESTINATION.getName(),
               SCHEDULER_GROUP.getName(),
               CUBE.getName(),
               CUBE_DIMENSION.getName(),
               CUBE_BUILD_PROCESS.getName(),
               MEASURE_FOLDER.getName(),
               ASSEMBLY.getName(),
               JAVA_DATA.getName()
            )
         );
         return !existingTypeNames.isEmpty();
      }
   }
}
