package org.flywaydb.core.internal.command;

import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

public class DbSchemas {
   private static final Log LOG = LogFactory.getLog(DbSchemas.class);
   private final Connection connection;
   private final Schema[] schemas;
   private final SchemaHistory schemaHistory;
   private final Database database;
   private final CallbackExecutor callbackExecutor;

   public DbSchemas(Database database, Schema[] schemas, SchemaHistory schemaHistory, CallbackExecutor callbackExecutor) {
      this.database = database;
      this.connection = database.getMainConnection();
      this.schemas = schemas;
      this.schemaHistory = schemaHistory;
      this.callbackExecutor = callbackExecutor;
   }

   public void create(boolean baseline) {
      this.callbackExecutor.onEvent(Event.CREATE_SCHEMA);
      int retries = 0;

      while(true) {
         try {
            ExecutionTemplateFactory.createExecutionTemplate(this.connection.getJdbcConnection(), this.database)
               .execute(
                  () -> {
                     List<Schema> createdSchemas = new ArrayList();
      
                     for(Schema schema : this.schemas) {
                        if (!schema.exists()) {
                           if (schema.getName() == null) {
                              throw new FlywayException(
                                 "Unable to determine schema for the schema history table. Set a default schema for the connection or specify one using the defaultSchema property!"
                              );
                           }
      
                           LOG.debug("Creating schema: " + schema);
                           schema.create();
                           createdSchemas.add(schema);
                        } else {
                           LOG.debug("Skipping creation of existing schema: " + schema);
                        }
                     }
      
                     if (!createdSchemas.isEmpty()) {
                        this.schemaHistory.create(baseline);
                        this.schemaHistory.addSchemasMarker((Schema[])createdSchemas.toArray(new Schema[0]));
                     }
      
                     return null;
                  }
               );
            return;
         } catch (RuntimeException var6) {
            if (++retries >= 10) {
               throw var6;
            }

            try {
               LOG.debug("Schema creation failed. Retrying in 1 sec ...");
               Thread.sleep(1000L);
            } catch (InterruptedException var5) {
            }
         }
      }
   }
}
