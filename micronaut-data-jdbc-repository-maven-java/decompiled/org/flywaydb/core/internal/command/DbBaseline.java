package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.BaselineResult;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

public class DbBaseline {
   private static final Log LOG = LogFactory.getLog(DbBaseline.class);
   private final SchemaHistory schemaHistory;
   private final MigrationVersion baselineVersion;
   private final String baselineDescription;
   private final CallbackExecutor callbackExecutor;
   private final BaselineResult baselineResult;

   public DbBaseline(
      SchemaHistory schemaHistory, MigrationVersion baselineVersion, String baselineDescription, CallbackExecutor callbackExecutor, Database database
   ) {
      this.schemaHistory = schemaHistory;
      this.baselineVersion = baselineVersion;
      this.baselineDescription = baselineDescription;
      this.callbackExecutor = callbackExecutor;
      this.baselineResult = CommandResultFactory.createBaselineResult(database.getCatalog());
   }

   public BaselineResult baseline() {
      this.callbackExecutor.onEvent(Event.BEFORE_BASELINE);

      try {
         if (!this.schemaHistory.exists()) {
            this.schemaHistory.create(true);
            LOG.info("Successfully baselined schema with version: " + this.baselineVersion);
            this.baselineResult.successfullyBaselined = true;
            this.baselineResult.baselineVersion = this.baselineVersion.toString();
         } else {
            AppliedMigration baselineMarker = this.schemaHistory.getBaselineMarker();
            if (baselineMarker == null) {
               if (this.schemaHistory.hasSchemasMarker() && this.baselineVersion.equals(MigrationVersion.fromVersion("0"))) {
                  throw new FlywayException(
                     "Unable to baseline schema history table " + this.schemaHistory + " with version 0 as this version was used for schema creation"
                  );
               }

               if (this.schemaHistory.hasNonSyntheticAppliedMigrations()) {
                  throw new FlywayException(
                     "Unable to baseline schema history table "
                        + this.schemaHistory
                        + " as it already contains migrations\nNeed to reset your baseline? Learn more: "
                        + "https://rd.gt/3CdwkXD"
                  );
               }

               if (this.schemaHistory.allAppliedMigrations().isEmpty()) {
                  throw new FlywayException(
                     "Unable to baseline schema history table "
                        + this.schemaHistory
                        + " as it already exists, and is empty.\nDelete the schema history table with the clean command, and run baseline again."
                  );
               }

               throw new FlywayException(
                  "Unable to baseline schema history table "
                     + this.schemaHistory
                     + " as it already contains migrations.\nDelete the schema history table with the clean command, and run baseline again.\nNeed to reset your baseline? Learn more: "
                     + "https://rd.gt/3CdwkXD"
               );
            }

            if (!this.baselineVersion.equals(baselineMarker.getVersion()) || !this.baselineDescription.equals(baselineMarker.getDescription())) {
               throw new FlywayException(
                  "Unable to baseline schema history table "
                     + this.schemaHistory
                     + " with ("
                     + this.baselineVersion
                     + ","
                     + this.baselineDescription
                     + ") as it has already been baselined with ("
                     + baselineMarker.getVersion()
                     + ","
                     + baselineMarker.getDescription()
                     + ")\nNeed to reset your baseline? Learn more: "
                     + "https://rd.gt/3CdwkXD"
               );
            }

            LOG.info(
               "Schema history table "
                  + this.schemaHistory
                  + " already initialized with ("
                  + this.baselineVersion
                  + ","
                  + this.baselineDescription
                  + "). Skipping."
            );
            this.baselineResult.successfullyBaselined = true;
            this.baselineResult.baselineVersion = this.baselineVersion.toString();
         }
      } catch (FlywayException var2) {
         this.callbackExecutor.onEvent(Event.AFTER_BASELINE_ERROR);
         this.baselineResult.successfullyBaselined = false;
         throw var2;
      }

      this.callbackExecutor.onEvent(Event.AFTER_BASELINE);
      return this.baselineResult;
   }
}
