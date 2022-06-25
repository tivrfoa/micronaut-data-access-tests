package org.flywaydb.core.internal.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.InfoResult;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.Pair;

public class MigrationInfoServiceImpl implements MigrationInfoService, OperationResult {
   private final MigrationResolver migrationResolver;
   private final Configuration configuration;
   private final Database database;
   private final Context context;
   private final SchemaHistory schemaHistory;
   private final MigrationVersion target;
   private final MigrationPattern[] cherryPick;
   private final boolean outOfOrder;
   private final boolean pending;
   private final boolean missing;
   private final boolean ignored;
   private final boolean future;
   private List<MigrationInfoImpl> migrationInfos;
   private Boolean allSchemasEmpty;

   public MigrationInfoServiceImpl(
      MigrationResolver migrationResolver,
      SchemaHistory schemaHistory,
      Database database,
      Configuration configuration,
      MigrationVersion target,
      boolean outOfOrder,
      MigrationPattern[] cherryPick,
      boolean pending,
      boolean missing,
      boolean ignored,
      boolean future
   ) {
      this.migrationResolver = migrationResolver;
      this.schemaHistory = schemaHistory;
      this.configuration = configuration;
      this.context = () -> configuration;
      this.database = database;
      this.target = target;
      this.outOfOrder = outOfOrder;
      this.cherryPick = cherryPick;
      this.pending = pending;
      this.missing = missing;
      this.ignored = ignored || cherryPick != null;
      this.future = future;
   }

   public void refresh() {
      Collection<ResolvedMigration> resolvedMigrations = this.migrationResolver.resolveMigrations(this.context);
      List<AppliedMigration> appliedMigrations = this.schemaHistory.allAppliedMigrations();
      MigrationInfoContext context = new MigrationInfoContext();
      context.outOfOrder = this.outOfOrder;
      context.pending = this.pending;
      context.missing = this.missing;
      context.ignored = this.ignored;
      context.future = this.future;
      context.ignorePatterns = this.configuration.getIgnoreMigrationPatterns();
      context.target = this.target;
      context.cherryPick = this.cherryPick;
      Map<Pair<MigrationVersion, Boolean>, ResolvedMigration> resolvedVersioned = new TreeMap();
      Map<String, ResolvedMigration> resolvedRepeatable = new TreeMap();
      ResolvedMigration pendingBaselineMigration = null;
      AppliedMigration appliedBaselineMigration = null;

      for(ResolvedMigration resolvedMigration : resolvedMigrations) {
         MigrationVersion version = resolvedMigration.getVersion();
         if (version != null) {
            if (version.compareTo(context.lastResolved) > 0) {
               context.lastResolved = version;
            }

            if (!resolvedMigration.getType().isBaselineMigration() || version.compareTo(context.latestBaselineMigration) <= 0) {
               resolvedVersioned.put(Pair.of(version, false), resolvedMigration);
            }
         } else {
            resolvedRepeatable.put(resolvedMigration.getDescription(), resolvedMigration);
         }
      }

      List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned = new ArrayList();
      List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatable = new ArrayList();

      for(AppliedMigration appliedMigration : appliedMigrations) {
         MigrationVersion version = appliedMigration.getVersion();
         if (version == null) {
            appliedRepeatable.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));
            if (appliedMigration.getType().equals(MigrationType.DELETE) && appliedMigration.isSuccess()) {
               this.markRepeatableAsDeleted(appliedMigration.getDescription(), appliedRepeatable);
            }
         } else {
            if (appliedMigration.getType() == MigrationType.SCHEMA) {
               context.schema = version;
            }

            if (appliedMigration.getType() == MigrationType.BASELINE) {
               context.baseline = version;
            }

            if (appliedMigration.getType().equals(MigrationType.DELETE) && appliedMigration.isSuccess()) {
               this.markAsDeleted(version, appliedVersioned);
            }

            appliedVersioned.add(Pair.of(appliedMigration, new AppliedMigrationAttributes()));
         }
      }

      for(Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
         AppliedMigration appliedMigration = av.getLeft();
         MigrationVersion version = appliedMigration.getVersion();
         if (version != null) {
            if (version.compareTo(context.lastApplied) > 0) {
               if (av.getLeft().getType() != MigrationType.DELETE && !av.getRight().deleted) {
                  context.lastApplied = version;
               }
            } else {
               av.getRight().outOfOrder = true;
            }
         }
      }

      if (MigrationVersion.CURRENT == this.target) {
         context.target = context.lastApplied;
      }

      List<MigrationInfoImpl> migrationInfos1 = new ArrayList();
      Set<ResolvedMigration> pendingResolvedVersioned = new HashSet(resolvedVersioned.values());

      for(Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedVersioned) {
         ResolvedMigration resolvedMigration = (ResolvedMigration)resolvedVersioned.get(Pair.of(av.getLeft().getVersion(), av.getLeft().getType().isUndo()));
         if (resolvedMigration != null && !av.getRight().deleted && av.getLeft().getType() != MigrationType.DELETE) {
            pendingResolvedVersioned.remove(resolvedMigration);
         }

         migrationInfos1.add(
            new MigrationInfoImpl(resolvedMigration, av.getLeft(), context, av.getRight().outOfOrder, av.getRight().deleted, av.getRight().undone)
         );
      }

      for(ResolvedMigration prv : pendingResolvedVersioned) {
         if (prv.getVersion().compareTo(context.latestBaselineMigration) > 0) {
            migrationInfos1.add(new MigrationInfoImpl(prv, null, context, false, false, false));
         }
      }

      if (pendingBaselineMigration != null) {
         migrationInfos1.add(new MigrationInfoImpl(pendingBaselineMigration, null, context, false, false, false));
      }

      if (this.configuration.isFailOnMissingTarget()
         && this.target != null
         && this.target != MigrationVersion.CURRENT
         && this.target != MigrationVersion.LATEST
         && this.target != MigrationVersion.NEXT) {
         boolean targetFound = false;

         for(MigrationInfoImpl migration : migrationInfos1) {
            if (this.target.compareTo(migration.getVersion()) == 0) {
               targetFound = true;
               break;
            }
         }

         if (!targetFound) {
            throw new FlywayException(
               "No migration with a target version " + this.target + " could be found. Ensure target is specified correctly and the migration exists."
            );
         }
      }

      for(Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedRepeatable) {
         if (!av.getRight().deleted || av.getLeft().getType() != MigrationType.DELETE) {
            AppliedMigration appliedRepeatableMigration = av.getLeft();
            String desc = appliedRepeatableMigration.getDescription();
            int rank = appliedRepeatableMigration.getInstalledRank();
            Map<String, Integer> latestRepeatableRuns = context.latestRepeatableRuns;
            if (!latestRepeatableRuns.containsKey(desc) || rank > latestRepeatableRuns.get(desc)) {
               latestRepeatableRuns.put(desc, rank);
            }
         }
      }

      Set<ResolvedMigration> pendingResolvedRepeatable = new HashSet(resolvedRepeatable.values());

      for(Pair<AppliedMigration, AppliedMigrationAttributes> av : appliedRepeatable) {
         AppliedMigration appliedRepeatableMigration = av.getLeft();
         String desc = appliedRepeatableMigration.getDescription();
         int rank = appliedRepeatableMigration.getInstalledRank();
         ResolvedMigration resolvedMigration = (ResolvedMigration)resolvedRepeatable.get(desc);
         int latestRank = context.latestRepeatableRuns.get(desc);
         if (!av.getRight().deleted
            && av.getLeft().getType() != MigrationType.DELETE
            && resolvedMigration != null
            && rank == latestRank
            && resolvedMigration.checksumMatches(appliedRepeatableMigration.getChecksum())) {
            pendingResolvedRepeatable.remove(resolvedMigration);
         }

         migrationInfos1.add(new MigrationInfoImpl(resolvedMigration, appliedRepeatableMigration, context, false, av.getRight().deleted, false));
      }

      for(ResolvedMigration prr : pendingResolvedRepeatable) {
         migrationInfos1.add(new MigrationInfoImpl(prr, null, context, false, false, false));
      }

      Collections.sort(migrationInfos1);
      this.migrationInfos = migrationInfos1;
      if (context.target == MigrationVersion.NEXT) {
         MigrationInfo[] pendingMigrationInfos = this.pending();
         if (pendingMigrationInfos.length == 0) {
            context.target = null;
         } else {
            context.target = pendingMigrationInfos[0].getVersion();
         }
      }

   }

   private void markRepeatableAsDeleted(String description, List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedRepeatable) {
      for(int i = appliedRepeatable.size() - 1; i >= 0; --i) {
         Pair<AppliedMigration, AppliedMigrationAttributes> ar = (Pair)appliedRepeatable.get(i);
         if (!ar.getLeft().getType().isSynthetic() && description.equals(ar.getLeft().getDescription())) {
            if (!ar.getRight().deleted) {
               ar.getRight().deleted = true;
            }

            return;
         }
      }

   }

   private void markAsDeleted(MigrationVersion version, List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedVersioned) {
      for(int i = appliedVersioned.size() - 1; i >= 0; --i) {
         Pair<AppliedMigration, AppliedMigrationAttributes> av = (Pair)appliedVersioned.get(i);
         if (!av.getLeft().getType().isSynthetic() && version.equals(av.getLeft().getVersion())) {
            if (av.getRight().deleted) {
               throw new FlywayException("Corrupted schema history: multiple delete entries for version " + version, ErrorCode.DUPLICATE_DELETED_MIGRATION);
            }

            av.getRight().deleted = true;
            return;
         }
      }

   }

   @Override
   public MigrationInfo[] all() {
      return (MigrationInfo[])this.migrationInfos.toArray(new MigrationInfo[0]);
   }

   @Override
   public MigrationInfo current() {
      MigrationInfo current = null;

      for(MigrationInfoImpl migrationInfo : this.migrationInfos) {
         if (migrationInfo.getState().isApplied()
            && !MigrationState.DELETED.equals(migrationInfo.getState())
            && !migrationInfo.getType().equals(MigrationType.DELETE)
            && migrationInfo.getVersion() != null
            && (current == null || migrationInfo.getVersion().compareTo(current.getVersion()) > 0)) {
            current = migrationInfo;
         }
      }

      if (current != null) {
         return current;
      } else {
         for(int i = this.migrationInfos.size() - 1; i >= 0; --i) {
            MigrationInfoImpl migrationInfo = (MigrationInfoImpl)this.migrationInfos.get(i);
            if (migrationInfo.getState().isApplied()
               && !MigrationState.DELETED.equals(migrationInfo.getState())
               && !migrationInfo.getType().equals(MigrationType.DELETE)) {
               return migrationInfo;
            }
         }

         return null;
      }
   }

   public MigrationInfoImpl[] pending() {
      List<MigrationInfoImpl> pendingMigrations = new ArrayList();

      for(MigrationInfoImpl migrationInfo : this.migrationInfos) {
         if (MigrationState.PENDING == migrationInfo.getState()) {
            pendingMigrations.add(migrationInfo);
         }
      }

      return (MigrationInfoImpl[])pendingMigrations.toArray(new MigrationInfoImpl[0]);
   }

   public MigrationInfoImpl[] applied() {
      List<MigrationInfoImpl> appliedMigrations = new ArrayList();

      for(MigrationInfoImpl migrationInfo : this.migrationInfos) {
         if (migrationInfo.getState().isApplied()) {
            appliedMigrations.add(migrationInfo);
         }
      }

      return (MigrationInfoImpl[])appliedMigrations.toArray(new MigrationInfoImpl[0]);
   }

   public MigrationInfo[] resolved() {
      List<MigrationInfo> resolvedMigrations = new ArrayList();

      for(MigrationInfo migrationInfo : this.migrationInfos) {
         if (migrationInfo.getState().isResolved()) {
            resolvedMigrations.add(migrationInfo);
         }
      }

      return (MigrationInfo[])resolvedMigrations.toArray(new MigrationInfo[0]);
   }

   public MigrationInfoImpl[] failed() {
      List<MigrationInfoImpl> failedMigrations = new ArrayList();

      for(MigrationInfoImpl migrationInfo : this.migrationInfos) {
         if (migrationInfo.getState().isFailed()) {
            failedMigrations.add(migrationInfo);
         }
      }

      return (MigrationInfoImpl[])failedMigrations.toArray(new MigrationInfoImpl[0]);
   }

   public MigrationInfo[] future() {
      List<MigrationInfo> futureMigrations = new ArrayList();

      for(MigrationInfo migrationInfo : this.migrationInfos) {
         if (migrationInfo.getState() == MigrationState.FUTURE_SUCCESS || migrationInfo.getState() == MigrationState.FUTURE_FAILED) {
            futureMigrations.add(migrationInfo);
         }
      }

      return (MigrationInfo[])futureMigrations.toArray(new MigrationInfo[0]);
   }

   public MigrationInfo[] outOfOrder() {
      List<MigrationInfo> outOfOrderMigrations = new ArrayList();

      for(MigrationInfo migrationInfo : this.migrationInfos) {
         if (migrationInfo.getState() == MigrationState.OUT_OF_ORDER) {
            outOfOrderMigrations.add(migrationInfo);
         }
      }

      return (MigrationInfo[])outOfOrderMigrations.toArray(new MigrationInfo[0]);
   }

   public List<ValidateOutput> validate() {
      List<ValidateOutput> invalidMigrations = new ArrayList();

      for(MigrationInfoImpl migrationInfo : this.migrationInfos) {
         ErrorDetails validateError = migrationInfo.validate();
         if (validateError != null) {
            invalidMigrations.add(CommandResultFactory.createValidateOutput(migrationInfo, validateError));
         }
      }

      return invalidMigrations;
   }

   public void setAllSchemasEmpty(Schema[] schemas) {
      this.allSchemasEmpty = Arrays.stream(schemas).filter(Schema::exists).allMatch(Schema::empty);
   }

   @Override
   public InfoResult getInfoResult() {
      return this.getInfoResult(this.all());
   }

   public InfoResult getInfoResult(MigrationInfo[] infos) {
      return CommandResultFactory.createInfoResult(this.context.getConfiguration(), this.database, infos, this.current(), this.allSchemasEmpty);
   }
}
