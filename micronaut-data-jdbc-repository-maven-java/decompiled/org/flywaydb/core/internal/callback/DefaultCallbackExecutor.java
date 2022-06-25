package org.flywaydb.core.internal.callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;

public class DefaultCallbackExecutor implements CallbackExecutor {
   private final Configuration configuration;
   private final Database database;
   private final Schema schema;
   private final List<Callback> callbacks;
   private MigrationInfo migrationInfo;

   public DefaultCallbackExecutor(Configuration configuration, Database database, Schema schema, Collection<Callback> callbacks) {
      this.configuration = configuration;
      this.database = database;
      this.schema = schema;
      this.callbacks = new ArrayList(callbacks);
      this.callbacks.sort(Comparator.comparing(Callback::getCallbackName));
   }

   @Override
   public void onEvent(Event event) {
      this.execute(event, this.database.getMainConnection());
   }

   @Override
   public void onMigrateOrUndoEvent(Event event) {
      this.execute(event, this.database.getMigrationConnection());
   }

   @Override
   public void setMigrationInfo(MigrationInfo migrationInfo) {
      this.migrationInfo = migrationInfo;
   }

   @Override
   public void onEachMigrateOrUndoEvent(Event event) {
      Context context = new SimpleContext(this.configuration, this.database.getMigrationConnection(), this.migrationInfo, null);

      for(Callback callback : this.callbacks) {
         if (callback.supports(event, context)) {
            callback.handle(event, context);
         }
      }

   }

   @Override
   public void onOperationFinishEvent(Event event, OperationResult operationResult) {
      Context context = new SimpleContext(this.configuration, this.database.getMigrationConnection(), this.migrationInfo, operationResult);

      for(Callback callback : this.callbacks) {
         if (callback.supports(event, context)) {
            callback.handle(event, context);
         }
      }

   }

   private void execute(Event event, Connection connection) {
      Context context = new SimpleContext(this.configuration, connection, null, null);

      for(Callback callback : this.callbacks) {
         if (callback.supports(event, context)) {
            if (callback.canHandleInTransaction(event, context)) {
               ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(), this.database).execute(() -> {
                  this.execute(connection, callback, event, context);
                  return null;
               });
            } else {
               this.execute(connection, callback, event, context);
            }
         }
      }

   }

   private void execute(Connection connection, Callback callback, Event event, Context context) {
      connection.restoreOriginalState();
      connection.changeCurrentSchemaTo(this.schema);
      this.handleEvent(callback, event, context);
   }

   private void handleEvent(Callback callback, Event event, Context context) {
      try {
         callback.handle(event, context);
      } catch (RuntimeException var5) {
         throw new FlywayException("Error while executing " + event.getId() + " callback: " + var5.getMessage(), var5);
      }
   }
}
