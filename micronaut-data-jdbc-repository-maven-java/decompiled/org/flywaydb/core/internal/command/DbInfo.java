package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

public class DbInfo {
   private final MigrationResolver migrationResolver;
   private final SchemaHistory schemaHistory;
   private final Configuration configuration;
   private final Database database;
   private final CallbackExecutor callbackExecutor;
   private final Schema[] schemas;

   public MigrationInfoService info() {
      this.callbackExecutor.onEvent(Event.BEFORE_INFO);

      MigrationInfoServiceImpl migrationInfoService;
      try {
         migrationInfoService = new MigrationInfoServiceImpl(
            this.migrationResolver,
            this.schemaHistory,
            this.database,
            this.configuration,
            this.configuration.getTarget(),
            this.configuration.isOutOfOrder(),
            this.configuration.getCherryPick(),
            true,
            true,
            true,
            true
         );
         migrationInfoService.refresh();
         migrationInfoService.setAllSchemasEmpty(this.schemas);
      } catch (FlywayException var3) {
         this.callbackExecutor.onEvent(Event.AFTER_INFO_ERROR);
         throw var3;
      }

      this.callbackExecutor.onEvent(Event.AFTER_INFO);
      return migrationInfoService;
   }

   public DbInfo(
      MigrationResolver migrationResolver,
      SchemaHistory schemaHistory,
      Configuration configuration,
      Database database,
      CallbackExecutor callbackExecutor,
      Schema[] schemas
   ) {
      this.migrationResolver = migrationResolver;
      this.schemaHistory = schemaHistory;
      this.configuration = configuration;
      this.database = database;
      this.callbackExecutor = callbackExecutor;
      this.schemas = schemas;
   }
}
