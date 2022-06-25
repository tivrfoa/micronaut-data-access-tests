package org.flywaydb.core.internal.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.CleanResult;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;

public class DbClean {
   private static final Log LOG = LogFactory.getLog(DbClean.class);
   private final SchemaHistory schemaHistory;
   protected final Schema defaultSchema;
   protected final Schema[] schemas;
   protected final Connection connection;
   protected final Database database;
   protected final CallbackExecutor callbackExecutor;
   protected final Configuration configuration;

   public DbClean(
      Database database, SchemaHistory schemaHistory, Schema defaultSchema, Schema[] schemas, CallbackExecutor callbackExecutor, Configuration configuration
   ) {
      this.schemaHistory = schemaHistory;
      this.defaultSchema = defaultSchema;
      this.schemas = schemas;
      this.connection = database.getMainConnection();
      this.database = database;
      this.callbackExecutor = callbackExecutor;
      this.configuration = configuration;
   }

   public CleanResult clean() throws FlywayException {
      if (this.configuration.isCleanDisabled()) {
         throw new FlywayException("Unable to execute clean as it has been disabled with the 'flyway.cleanDisabled' property.");
      } else {
         this.callbackExecutor.onEvent(Event.BEFORE_CLEAN);
         CleanResult cleanResult = CommandResultFactory.createCleanResult(this.database.getCatalog());
         this.clean(cleanResult);
         this.callbackExecutor.onEvent(Event.AFTER_CLEAN);
         this.schemaHistory.clearCache();
         return cleanResult;
      }
   }

   protected void clean(CleanResult cleanResult) {
      this.clean(this.defaultSchema, this.schemas, cleanResult);
   }

   protected void clean(Schema defaultSchema, Schema[] schemas, CleanResult cleanResult) {
      try {
         this.connection.changeCurrentSchemaTo(defaultSchema);
         List<String> dropSchemas = new ArrayList();

         try {
            dropSchemas = this.schemaHistory.getSchemasCreatedByFlyway();
         } catch (Exception var6) {
            LOG.error("Error while checking whether the schemas should be dropped. Schemas will not be dropped", var6);
         }

         this.clean(schemas, cleanResult, dropSchemas);
      } catch (FlywayException var7) {
         this.callbackExecutor.onEvent(Event.AFTER_CLEAN_ERROR);
         throw var7;
      }
   }

   protected void clean(Schema[] schemas, CleanResult cleanResult, List<String> dropSchemas) {
      this.dropDatabaseObjectsPreSchemas();
      List<Schema> schemaList = new ArrayList(Arrays.asList(schemas));
      int i = 0;

      while(i < schemaList.size()) {
         Schema schema = (Schema)schemaList.get(i);
         if (!schema.exists()) {
            String unknownSchemaWarning = "Unable to clean unknown schema: " + schema;
            cleanResult.addWarning(unknownSchemaWarning);
            LOG.warn(unknownSchemaWarning);
            schemaList.remove(i);
         } else {
            ++i;
         }
      }

      this.cleanSchemas((Schema[])schemaList.toArray(new Schema[0]), dropSchemas, cleanResult);
      Collections.reverse(schemaList);
      this.cleanSchemas((Schema[])schemaList.toArray(new Schema[0]), dropSchemas, null);
      this.dropDatabaseObjectsPostSchemas(schemas);

      for(Schema schema : schemas) {
         if (dropSchemas.contains(schema.getName())) {
            this.dropSchema(schema, cleanResult);
         }
      }

   }

   private void dropDatabaseObjectsPreSchemas() {
      LOG.debug("Dropping pre-schema database level objects...");
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();

      try {
         ExecutionTemplateFactory.createExecutionTemplate(this.connection.getJdbcConnection(), this.database).execute(() -> {
            this.database.cleanPreSchemas();
            return null;
         });
         stopWatch.stop();
         LOG.info(
            String.format("Successfully dropped pre-schema database level objects (execution time %s)", TimeFormat.format(stopWatch.getTotalTimeMillis()))
         );
      } catch (FlywaySqlException var3) {
         LOG.debug(var3.getMessage());
         LOG.warn("Unable to drop pre-schema database level objects");
      }

   }

   private void dropDatabaseObjectsPostSchemas(Schema[] schemas) {
      LOG.debug("Dropping post-schema database level objects...");
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();

      try {
         ExecutionTemplateFactory.createExecutionTemplate(this.connection.getJdbcConnection(), this.database).execute(() -> {
            this.database.cleanPostSchemas(schemas);
            return null;
         });
         stopWatch.stop();
         LOG.info(
            String.format("Successfully dropped post-schema database level objects (execution time %s)", TimeFormat.format(stopWatch.getTotalTimeMillis()))
         );
      } catch (FlywaySqlException var4) {
         LOG.debug(var4.getMessage());
         LOG.warn("Unable to drop post-schema database level objects");
      }

   }

   private void dropSchema(Schema schema, CleanResult cleanResult) {
      LOG.debug("Dropping schema " + schema + "...");
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();

      try {
         ExecutionTemplateFactory.createExecutionTemplate(this.connection.getJdbcConnection(), this.database).execute(() -> {
            schema.drop();
            return null;
         });
         cleanResult.schemasDropped.add(schema.getName());
         stopWatch.stop();
         LOG.info(String.format("Successfully dropped schema %s (execution time %s)", schema, TimeFormat.format(stopWatch.getTotalTimeMillis())));
      } catch (FlywaySqlException var5) {
         LOG.debug(var5.getMessage());
         LOG.warn("Unable to drop schema " + schema + ". It was cleaned instead.");
         cleanResult.schemasCleaned.add(schema.getName());
      }

   }

   private void cleanSchemas(Schema[] schemas, List<String> dropSchemas, CleanResult cleanResult) {
      for(Schema schema : schemas) {
         if (dropSchemas.contains(schema.getName())) {
            try {
               this.cleanSchema(schema);
            } catch (FlywayException var9) {
            }
         } else {
            this.cleanSchema(schema);
            if (cleanResult != null) {
               cleanResult.schemasCleaned.add(schema.getName());
            }
         }
      }

   }

   private void cleanSchema(Schema schema) {
      LOG.debug("Cleaning schema " + schema + "...");
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      this.doCleanSchema(schema);
      stopWatch.stop();
      LOG.info(String.format("Successfully cleaned schema %s (execution time %s)", schema, TimeFormat.format(stopWatch.getTotalTimeMillis())));
   }

   protected void doCleanSchema(Schema schema) {
      ExecutionTemplateFactory.createExecutionTemplate(this.connection.getJdbcConnection(), this.database).execute(() -> {
         schema.clean();
         return null;
      });
   }
}
