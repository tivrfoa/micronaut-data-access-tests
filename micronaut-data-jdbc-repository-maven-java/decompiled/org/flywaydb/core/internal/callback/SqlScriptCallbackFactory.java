package org.flywaydb.core.internal.callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

public class SqlScriptCallbackFactory {
   private static final Log LOG = LogFactory.getLog(SqlScriptCallbackFactory.class);
   private final List<SqlScriptCallbackFactory.SqlScriptCallback> callbacks = new ArrayList();

   public SqlScriptCallbackFactory(
      ResourceProvider resourceProvider, SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScriptFactory sqlScriptFactory, Configuration configuration
   ) {
      Map<String, SqlScript> callbacksFound = new HashMap();
      LOG.debug("Scanning for SQL callbacks ...");
      Collection<LoadableResource> resources = resourceProvider.getResources("", configuration.getSqlMigrationSuffixes());
      ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

      for(LoadableResource resource : resources) {
         ResourceName parsedName = resourceNameParser.parse(resource.getFilename());
         if (parsedName.isValid()) {
            String name = parsedName.getFilenameWithoutSuffix();
            Event event = Event.fromId(parsedName.getPrefix());
            if (event != null) {
               SqlScript existing = (SqlScript)callbacksFound.get(name);
               if (existing != null) {
                  throw new FlywayException(
                     "Found more than 1 SQL callback script called "
                        + name
                        + "!\nOffenders:\n-> "
                        + existing.getResource().getAbsolutePathOnDisk()
                        + "\n-> "
                        + resource.getAbsolutePathOnDisk()
                  );
               }

               SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource, configuration.isMixed(), resourceProvider);
               callbacksFound.put(name, sqlScript);
               boolean batch = false;
               this.callbacks
                  .add(new SqlScriptCallbackFactory.SqlScriptCallback(event, parsedName.getDescription(), sqlScriptExecutorFactory, sqlScript, batch));
            }
         }
      }

      Collections.sort(this.callbacks);
   }

   public List<Callback> getCallbacks() {
      return new ArrayList(this.callbacks);
   }

   private static class SqlScriptCallback implements Callback, Comparable<SqlScriptCallbackFactory.SqlScriptCallback> {
      private final Event event;
      private final String description;
      private final SqlScriptExecutorFactory sqlScriptExecutorFactory;
      private final SqlScript sqlScript;
      private final boolean batch;

      @Override
      public boolean supports(Event event, Context context) {
         return this.event == event;
      }

      @Override
      public boolean canHandleInTransaction(Event event, Context context) {
         return this.sqlScript.executeInTransaction();
      }

      @Override
      public void handle(Event event, Context context) {
         if (!this.sqlScript.shouldExecute()) {
            SqlScriptCallbackFactory.LOG.debug("Not executing SQL callback: " + event.getId() + (this.description == null ? "" : " - " + this.description));
         } else {
            SqlScriptCallbackFactory.LOG
               .info(
                  "Executing SQL callback: "
                     + event.getId()
                     + (this.description == null ? "" : " - " + this.description)
                     + (this.sqlScript.executeInTransaction() ? "" : " [non-transactional]")
               );
            boolean outputQueryResults = false;
            this.sqlScriptExecutorFactory.createSqlScriptExecutor(context.getConnection(), false, this.batch, outputQueryResults).execute(this.sqlScript);
         }
      }

      @Override
      public String getCallbackName() {
         return this.description;
      }

      public int compareTo(SqlScriptCallbackFactory.SqlScriptCallback o) {
         int result = this.event.compareTo(o.event);
         if (result == 0) {
            if (this.description == null) {
               return -1;
            }

            if (o.description == null) {
               return 1;
            }

            result = this.description.compareTo(o.description);
         }

         return result;
      }

      private SqlScriptCallback(Event event, String description, SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScript sqlScript, boolean batch) {
         this.event = event;
         this.description = description;
         this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
         this.sqlScript = sqlScript;
         this.batch = batch;
      }
   }
}
