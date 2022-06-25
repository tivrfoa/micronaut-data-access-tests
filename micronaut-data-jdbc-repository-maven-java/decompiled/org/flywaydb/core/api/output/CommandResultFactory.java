package org.flywaydb.core.api.output;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;

public class CommandResultFactory {
   public static InfoResult createInfoResult(
      Configuration configuration, Database database, MigrationInfo[] migrationInfos, MigrationInfo current, boolean allSchemasEmpty
   ) {
      String flywayVersion = VersionPrinter.getVersion();
      String databaseName = getDatabaseName(configuration, database);
      Set<MigrationInfo> undoableMigrations = getUndoMigrations(migrationInfos);
      List<InfoOutput> infoOutputs = new ArrayList();

      for(MigrationInfo migrationInfo : migrationInfos) {
         infoOutputs.add(createInfoOutput(undoableMigrations, migrationInfo));
      }

      MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
      MigrationVersion schemaVersionToOutput = currentSchemaVersion == null ? MigrationVersion.EMPTY : currentSchemaVersion;
      String schemaVersion = schemaVersionToOutput.getVersion();
      return new InfoResult(flywayVersion, databaseName, schemaVersion, String.join(", ", configuration.getSchemas()), infoOutputs, allSchemasEmpty);
   }

   public static MigrateResult createMigrateResult(String databaseName, Configuration configuration) {
      String flywayVersion = VersionPrinter.getVersion();
      return new MigrateResult(flywayVersion, databaseName, String.join(", ", configuration.getSchemas()));
   }

   public static CleanResult createCleanResult(String databaseName) {
      String flywayVersion = VersionPrinter.getVersion();
      return new CleanResult(flywayVersion, databaseName);
   }

   public static UndoResult createUndoResult(String databaseName, Configuration configuration) {
      String flywayVersion = VersionPrinter.getVersion();
      return new UndoResult(flywayVersion, databaseName, String.join(", ", configuration.getSchemas()));
   }

   public static BaselineResult createBaselineResult(String databaseName) {
      String flywayVersion = VersionPrinter.getVersion();
      return new BaselineResult(flywayVersion, databaseName);
   }

   public static ValidateResult createValidateResult(
      String databaseName, ErrorDetails validationError, int validationCount, List<ValidateOutput> invalidMigrations, List<String> warnings
   ) {
      String flywayVersion = VersionPrinter.getVersion();
      boolean validationSuccessful = validationError == null;
      List<ValidateOutput> invalidMigrationsList = (List<ValidateOutput>)(invalidMigrations == null ? new ArrayList() : invalidMigrations);
      return new ValidateResult(flywayVersion, databaseName, validationError, validationSuccessful, validationCount, invalidMigrationsList, warnings);
   }

   public static RepairResult createRepairResult(String databaseName) {
      String flywayVersion = VersionPrinter.getVersion();
      return new RepairResult(flywayVersion, databaseName);
   }

   public static InfoOutput createInfoOutput(Set<MigrationInfo> undoableMigrations, MigrationInfo migrationInfo) {
      return new InfoOutput(
         getCategory(migrationInfo),
         migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
         migrationInfo.getDescription(),
         migrationInfo.getType() != null ? migrationInfo.getType().toString() : "",
         migrationInfo.getInstalledOn() != null ? migrationInfo.getInstalledOn().toInstant().toString() : "",
         migrationInfo.getState().getDisplayName(),
         getUndoableStatus(migrationInfo, undoableMigrations),
         migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "",
         getUndoablePath(migrationInfo, undoableMigrations),
         migrationInfo.getInstalledBy() != null ? migrationInfo.getInstalledBy() : "",
         migrationInfo.getExecutionTime() != null ? migrationInfo.getExecutionTime() : 0
      );
   }

   public static MigrateOutput createMigrateOutput(MigrationInfo migrationInfo, int executionTime) {
      return new MigrateOutput(
         getCategory(migrationInfo),
         migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
         migrationInfo.getDescription(),
         migrationInfo.getType() != null ? migrationInfo.getType().toString() : "",
         migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "",
         executionTime
      );
   }

   public static UndoOutput createUndoOutput(ResolvedMigration migrationInfo, int executionTime) {
      return new UndoOutput(
         migrationInfo.getVersion().getVersion(),
         migrationInfo.getDescription(),
         migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "",
         executionTime
      );
   }

   public static ValidateOutput createValidateOutput(MigrationInfo migrationInfo, ErrorDetails validateError) {
      return new ValidateOutput(
         migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
         migrationInfo.getDescription(),
         migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "",
         validateError
      );
   }

   public static RepairOutput createRepairOutput(MigrationInfo migrationInfo) {
      return new RepairOutput(
         migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
         migrationInfo.getDescription(),
         migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : ""
      );
   }

   public static RepairOutput createRepairOutput(AppliedMigration am) {
      return new RepairOutput(am.getVersion() != null ? am.getVersion().getVersion() : "", am.getDescription(), "");
   }

   private static String getUndoableStatus(MigrationInfo migrationInfo, Set<MigrationInfo> undoableMigrations) {
      return "";
   }

   private static String getUndoablePath(MigrationInfo migrationInfo, Set<MigrationInfo> undoableMigrations) {
      return "";
   }

   private static Set<MigrationInfo> getUndoMigrations(MigrationInfo[] migrationInfos) {
      return Collections.emptySet();
   }

   private static MigrationInfo[] removeAvailableUndoMigrations(MigrationInfo[] migrationInfos) {
      return (MigrationInfo[])Arrays.stream(migrationInfos).filter(m -> !m.getState().equals(MigrationState.AVAILABLE)).toArray(x$0 -> new MigrationInfo[x$0]);
   }

   private static String getDatabaseName(Configuration configuration, Database database) {
      try {
         return database.getCatalog();
      } catch (Exception var9) {
         try {
            Connection connection = configuration.getDataSource().getConnection();

            String var5;
            try {
               String catalog = connection.getCatalog();
               var5 = catalog != null ? catalog : "";
            } catch (Throwable var7) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (connection != null) {
               connection.close();
            }

            return var5;
         } catch (Exception var8) {
            return "";
         }
      }
   }

   private static String getCategory(MigrationInfo migrationInfo) {
      if (migrationInfo.getType().isSynthetic()) {
         return "";
      } else {
         return migrationInfo.getVersion() == null ? "Repeatable" : "Versioned";
      }
   }
}
