package org.flywaydb.core.internal.schemahistory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.output.RepairResult;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.AbbreviationUtils;
import org.flywaydb.core.internal.util.StringUtils;

public abstract class SchemaHistory {
   public static final String NO_DESCRIPTION_MARKER = "<< no description >>";
   protected Table table;

   public abstract <T> T lock(Callable<T> var1);

   public abstract boolean exists();

   public abstract void create(boolean var1);

   public final boolean hasNonSyntheticAppliedMigrations() {
      for(AppliedMigration appliedMigration : this.allAppliedMigrations()) {
         if (!appliedMigration.getType().isSynthetic()) {
            return true;
         }
      }

      return false;
   }

   public abstract List<AppliedMigration> allAppliedMigrations();

   public final AppliedMigration getBaselineMarker() {
      List<AppliedMigration> appliedMigrations = this.allAppliedMigrations();

      for(int i = 0; i < Math.min(appliedMigrations.size(), 2); ++i) {
         AppliedMigration appliedMigration = (AppliedMigration)appliedMigrations.get(i);
         if (appliedMigration.getType() == MigrationType.BASELINE) {
            return appliedMigration;
         }
      }

      return null;
   }

   public abstract boolean removeFailedMigrations(RepairResult var1, MigrationPattern[] var2);

   public final void addSchemasMarker(Schema[] schemas) {
      this.addAppliedMigration(null, "<< Flyway Schema Creation >>", MigrationType.SCHEMA, StringUtils.arrayToCommaDelimitedString(schemas), null, 0, true);
   }

   public final boolean hasSchemasMarker() {
      List<AppliedMigration> appliedMigrations = this.allAppliedMigrations();
      return !appliedMigrations.isEmpty() && ((AppliedMigration)appliedMigrations.get(0)).getType() == MigrationType.SCHEMA;
   }

   public List<String> getSchemasCreatedByFlyway() {
      return (List<String>)(!this.hasSchemasMarker()
         ? new ArrayList()
         : (List)Arrays.stream(((AppliedMigration)this.allAppliedMigrations().get(0)).getScript().split(","))
            .map(result -> this.table.getDatabase().unQuote(result))
            .collect(Collectors.toList()));
   }

   public abstract void update(AppliedMigration var1, ResolvedMigration var2);

   public abstract void delete(AppliedMigration var1);

   public void clearCache() {
   }

   public final void addAppliedMigration(
      MigrationVersion version, String description, MigrationType type, String script, Integer checksum, int executionTime, boolean success
   ) {
      int installedRank = type == MigrationType.SCHEMA ? 0 : this.calculateInstalledRank();
      this.doAddAppliedMigration(
         installedRank,
         version,
         AbbreviationUtils.abbreviateDescription(description),
         type,
         AbbreviationUtils.abbreviateScript(script),
         checksum,
         executionTime,
         success
      );
   }

   protected int calculateInstalledRank() {
      List<AppliedMigration> appliedMigrations = this.allAppliedMigrations();
      return appliedMigrations.isEmpty() ? 1 : ((AppliedMigration)appliedMigrations.get(appliedMigrations.size() - 1)).getInstalledRank() + 1;
   }

   protected abstract void doAddAppliedMigration(
      int var1, MigrationVersion var2, String var3, MigrationType var4, String var5, Integer var6, int var7, boolean var8
   );

   public String toString() {
      return this.table.toString();
   }
}
