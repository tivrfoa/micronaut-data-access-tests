package org.flywaydb.core.internal.info;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.util.AbbreviationUtils;

public class MigrationInfoImpl implements MigrationInfo {
   private final ResolvedMigration resolvedMigration;
   private final AppliedMigration appliedMigration;
   private final MigrationInfoContext context;
   private final boolean outOfOrder;
   private final boolean deleted;
   private final boolean shouldNotExecuteMigration;

   MigrationInfoImpl(
      ResolvedMigration resolvedMigration, AppliedMigration appliedMigration, MigrationInfoContext context, boolean outOfOrder, boolean deleted, boolean undone
   ) {
      this.resolvedMigration = resolvedMigration;
      this.appliedMigration = appliedMigration;
      this.context = context;
      this.outOfOrder = outOfOrder;
      this.deleted = deleted;
      this.shouldNotExecuteMigration = this.shouldNotExecuteMigration(resolvedMigration);
   }

   public ResolvedMigration getResolvedMigration() {
      return this.resolvedMigration;
   }

   public AppliedMigration getAppliedMigration() {
      return this.appliedMigration;
   }

   @Override
   public MigrationType getType() {
      return this.appliedMigration != null ? this.appliedMigration.getType() : this.resolvedMigration.getType();
   }

   @Override
   public Integer getChecksum() {
      return this.appliedMigration != null ? this.appliedMigration.getChecksum() : this.resolvedMigration.getChecksum();
   }

   @Override
   public MigrationVersion getVersion() {
      return this.appliedMigration != null ? this.appliedMigration.getVersion() : this.resolvedMigration.getVersion();
   }

   @Override
   public String getDescription() {
      return this.appliedMigration != null ? this.appliedMigration.getDescription() : this.resolvedMigration.getDescription();
   }

   @Override
   public String getScript() {
      return this.appliedMigration != null ? this.appliedMigration.getScript() : this.resolvedMigration.getScript();
   }

   @Override
   public MigrationState getState() {
      if (this.deleted) {
         return MigrationState.DELETED;
      } else if (this.appliedMigration == null) {
         if (this.shouldNotExecuteMigration) {
            return MigrationState.IGNORED;
         } else {
            if (this.resolvedMigration.getVersion() != null) {
               if (this.resolvedMigration.getVersion().compareTo(this.context.baseline) < 0) {
                  return MigrationState.BELOW_BASELINE;
               }

               if (this.context.target != null
                  && this.context.target != MigrationVersion.NEXT
                  && this.resolvedMigration.getVersion().compareTo(this.context.target) > 0) {
                  return MigrationState.ABOVE_TARGET;
               }

               if (this.resolvedMigration.getVersion().compareTo(this.context.lastApplied) < 0 && !this.context.outOfOrder) {
                  return MigrationState.IGNORED;
               }

               if (this.resolvedMigration.getVersion().compareTo(this.context.latestBaselineMigration) < 0
                  || this.resolvedMigration.getVersion().compareTo(this.context.latestBaselineMigration) == 0
                     && !this.resolvedMigration.getType().isBaselineMigration()) {
                  return MigrationState.IGNORED;
               }
            }

            return MigrationState.PENDING;
         }
      } else if (MigrationType.DELETE == this.appliedMigration.getType()) {
         return MigrationState.SUCCESS;
      } else if (MigrationType.BASELINE == this.appliedMigration.getType()) {
         return MigrationState.BASELINE;
      } else if (this.resolvedMigration == null && this.isRepeatableLatest()) {
         if (MigrationType.SCHEMA == this.appliedMigration.getType()) {
            return MigrationState.SUCCESS;
         } else if (this.appliedMigration.getVersion() == null || this.getVersion().compareTo(this.context.lastResolved) < 0) {
            return this.appliedMigration.isSuccess() ? MigrationState.MISSING_SUCCESS : MigrationState.MISSING_FAILED;
         } else {
            return this.appliedMigration.isSuccess() ? MigrationState.FUTURE_SUCCESS : MigrationState.FUTURE_FAILED;
         }
      } else if (!this.appliedMigration.isSuccess()) {
         return MigrationState.FAILED;
      } else if (this.appliedMigration.getVersion() == null) {
         if (this.appliedMigration.getInstalledRank() == this.context.latestRepeatableRuns.get(this.appliedMigration.getDescription())) {
            return this.resolvedMigration != null && this.resolvedMigration.checksumMatches(this.appliedMigration.getChecksum())
               ? MigrationState.SUCCESS
               : MigrationState.OUTDATED;
         } else {
            return MigrationState.SUPERSEDED;
         }
      } else {
         return this.outOfOrder ? MigrationState.OUT_OF_ORDER : MigrationState.SUCCESS;
      }
   }

   private boolean isRepeatableLatest() {
      if (this.appliedMigration.getVersion() != null) {
         return true;
      } else {
         Integer latestRepeatableRank = (Integer)this.context.latestRepeatableRuns.get(this.appliedMigration.getDescription());
         return latestRepeatableRank == null || this.appliedMigration.getInstalledRank() == latestRepeatableRank;
      }
   }

   @Override
   public Date getInstalledOn() {
      return this.appliedMigration != null ? this.appliedMigration.getInstalledOn() : null;
   }

   @Override
   public String getInstalledBy() {
      return this.appliedMigration != null ? this.appliedMigration.getInstalledBy() : null;
   }

   @Override
   public Integer getInstalledRank() {
      return this.appliedMigration != null ? this.appliedMigration.getInstalledRank() : null;
   }

   @Override
   public Integer getExecutionTime() {
      return this.appliedMigration != null ? this.appliedMigration.getExecutionTime() : null;
   }

   @Override
   public String getPhysicalLocation() {
      return this.resolvedMigration != null ? this.resolvedMigration.getPhysicalLocation() : "";
   }

   public ErrorDetails validate() {
      MigrationState state = this.getState();
      if (MigrationState.ABOVE_TARGET.equals(state)) {
         return null;
      } else if (MigrationState.DELETED.equals(state)) {
         return null;
      } else if (Arrays.stream(this.context.ignorePatterns).anyMatch(p -> p.matchesMigration(this.getVersion() != null, state))) {
         return null;
      } else if (!state.isFailed() || this.context.future && MigrationState.FUTURE_FAILED == state) {
         if (this.resolvedMigration != null
            || this.appliedMigration.getType().isSynthetic()
            || MigrationState.SUPERSEDED == state
            || this.context.missing && (MigrationState.MISSING_SUCCESS == state || MigrationState.MISSING_FAILED == state)
            || this.context.future && (MigrationState.FUTURE_SUCCESS == state || MigrationState.FUTURE_FAILED == state)) {
            if (!this.context.ignored && MigrationState.IGNORED == state) {
               if (this.shouldNotExecuteMigration) {
                  return null;
               } else if (this.getVersion() != null) {
                  String errorMessage = "Detected resolved migration not applied to database: "
                     + this.getVersion()
                     + ".\nTo ignore this migration, set -ignoreIgnoredMigrations=true. To allow executing this migration, set -outOfOrder=true.";
                  return new ErrorDetails(ErrorCode.RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED, errorMessage);
               } else {
                  String errorMessage = "Detected resolved repeatable migration not applied to database: "
                     + this.getDescription()
                     + ".\nTo ignore this migration, set -ignoreIgnoredMigrations=true.";
                  return new ErrorDetails(ErrorCode.RESOLVED_REPEATABLE_MIGRATION_NOT_APPLIED, errorMessage);
               }
            } else if (!this.context.pending && MigrationState.PENDING == state) {
               if (this.getVersion() != null) {
                  String errorMessage = "Detected resolved migration not applied to database: "
                     + this.getVersion()
                     + ".\nTo fix this error, either run migrate, or set -ignorePendingMigrations=true.";
                  return new ErrorDetails(ErrorCode.RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED, errorMessage);
               } else {
                  String errorMessage = "Detected resolved repeatable migration not applied to database: "
                     + this.getDescription()
                     + ".\nTo fix this error, either run migrate, or set -ignorePendingMigrations=true.";
                  return new ErrorDetails(ErrorCode.RESOLVED_REPEATABLE_MIGRATION_NOT_APPLIED, errorMessage);
               }
            } else if (!this.context.pending && MigrationState.OUTDATED == state) {
               String errorMessage = "Detected outdated resolved repeatable migration that should be re-applied to database: "
                  + this.getDescription()
                  + ".\nRun migrate to execute this migration.";
               return new ErrorDetails(ErrorCode.OUTDATED_REPEATABLE_MIGRATION, errorMessage);
            } else {
               if (this.resolvedMigration != null && this.appliedMigration != null && this.getType() != MigrationType.DELETE) {
                  String migrationIdentifier = this.appliedMigration.getVersion() == null
                     ? this.appliedMigration.getScript()
                     : "version " + this.appliedMigration.getVersion();
                  if (this.getVersion() == null || this.getVersion().compareTo(this.context.baseline) > 0) {
                     if (this.resolvedMigration.getType() != this.appliedMigration.getType()) {
                        String mismatchMessage = this.createMismatchMessage(
                           "type", migrationIdentifier, this.appliedMigration.getType(), this.resolvedMigration.getType()
                        );
                        return new ErrorDetails(ErrorCode.TYPE_MISMATCH, mismatchMessage);
                     }

                     if ((
                           this.resolvedMigration.getVersion() != null
                              || this.context.pending && MigrationState.OUTDATED != state && MigrationState.SUPERSEDED != state
                        )
                        && !this.resolvedMigration.checksumMatches(this.appliedMigration.getChecksum())) {
                        String mismatchMessage = this.createMismatchMessage(
                           "checksum", migrationIdentifier, this.appliedMigration.getChecksum(), this.resolvedMigration.getChecksum()
                        );
                        return new ErrorDetails(ErrorCode.CHECKSUM_MISMATCH, mismatchMessage);
                     }

                     if (this.descriptionMismatch(this.resolvedMigration, this.appliedMigration)) {
                        String mismatchMessage = this.createMismatchMessage(
                           "description", migrationIdentifier, this.appliedMigration.getDescription(), this.resolvedMigration.getDescription()
                        );
                        return new ErrorDetails(ErrorCode.DESCRIPTION_MISMATCH, mismatchMessage);
                     }
                  }
               }

               if (!this.context.pending && MigrationState.PENDING == state && this.resolvedMigration instanceof ResolvedMigrationImpl) {
                  ((ResolvedMigrationImpl)this.resolvedMigration).validate();
               }

               return null;
            }
         } else if (this.appliedMigration.getVersion() != null) {
            String errorMessage = "Detected applied migration not resolved locally: "
               + this.getVersion()
               + ".\nIf you removed this migration intentionally, run repair to mark the migration as deleted.";
            return new ErrorDetails(ErrorCode.APPLIED_VERSIONED_MIGRATION_NOT_RESOLVED, errorMessage);
         } else {
            String errorMessage = "Detected applied migration not resolved locally: "
               + this.getDescription()
               + ".\nIf you removed this migration intentionally, run repair to mark the migration as deleted.";
            return new ErrorDetails(ErrorCode.APPLIED_REPEATABLE_MIGRATION_NOT_RESOLVED, errorMessage);
         }
      } else if (this.getVersion() == null) {
         String errorMessage = "Detected failed repeatable migration: "
            + this.getDescription()
            + ".\nPlease remove any half-completed changes then run repair to fix the schema history.";
         return new ErrorDetails(ErrorCode.FAILED_REPEATABLE_MIGRATION, errorMessage);
      } else {
         String errorMessage = "Detected failed migration to version "
            + this.getVersion()
            + " ("
            + this.getDescription()
            + ").\nPlease remove any half-completed changes then run repair to fix the schema history.";
         return new ErrorDetails(ErrorCode.FAILED_VERSIONED_MIGRATION, errorMessage);
      }
   }

   private boolean shouldNotExecuteMigration(ResolvedMigration resolvedMigration) {
      return resolvedMigration != null && resolvedMigration.getExecutor() != null && !resolvedMigration.getExecutor().shouldExecute();
   }

   private boolean descriptionMismatch(ResolvedMigration resolvedMigration, AppliedMigration appliedMigration) {
      if ("<< no description >>".equals(appliedMigration.getDescription())) {
         return !"".equals(resolvedMigration.getDescription());
      } else {
         return !AbbreviationUtils.abbreviateDescription(resolvedMigration.getDescription()).equals(appliedMigration.getDescription());
      }
   }

   private String createMismatchMessage(String mismatch, String migrationIdentifier, Object applied, Object resolved) {
      return String.format(
         "Migration "
            + mismatch
            + " mismatch for migration %s\n-> Applied to database : %s\n-> Resolved locally    : %s\nEither revert the changes to the migration, or run repair to update the schema history.",
         migrationIdentifier,
         applied,
         resolved
      );
   }

   public boolean canExecuteInTransaction() {
      return this.resolvedMigration != null && this.resolvedMigration.getExecutor().canExecuteInTransaction();
   }

   public int compareTo(MigrationInfo o) {
      if (this.getInstalledRank() != null && o.getInstalledRank() != null) {
         return this.getInstalledRank().compareTo(o.getInstalledRank());
      } else {
         MigrationState state = this.getState();
         MigrationState oState = o.getState();
         if (state == MigrationState.BELOW_BASELINE && oState.isApplied()) {
            return -1;
         } else if (state.isApplied() && oState == MigrationState.BELOW_BASELINE) {
            return 1;
         } else if (this.getInstalledRank() != null) {
            return -1;
         } else {
            return o.getInstalledRank() != null ? 1 : this.compareVersion(o);
         }
      }
   }

   @Override
   public int compareVersion(MigrationInfo o) {
      if (this.getVersion() != null && o.getVersion() != null) {
         int v = this.getVersion().compareTo(o.getVersion());
         return v != 0 ? v : 0;
      } else if (this.getVersion() != null) {
         return -1;
      } else {
         return o.getVersion() != null ? 1 : this.getDescription().compareTo(o.getDescription());
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MigrationInfoImpl that = (MigrationInfoImpl)o;
         if (!Objects.equals(this.appliedMigration, that.appliedMigration)) {
            return false;
         } else {
            return !this.context.equals(that.context) ? false : Objects.equals(this.resolvedMigration, that.resolvedMigration);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.resolvedMigration != null ? this.resolvedMigration.hashCode() : 0;
      result = 31 * result + (this.appliedMigration != null ? this.appliedMigration.hashCode() : 0);
      return 31 * result + this.context.hashCode();
   }
}
