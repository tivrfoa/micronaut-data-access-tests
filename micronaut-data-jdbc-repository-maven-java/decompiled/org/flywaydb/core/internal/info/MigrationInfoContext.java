package org.flywaydb.core.internal.info;

import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.pattern.ValidatePattern;

public class MigrationInfoContext {
   public boolean outOfOrder;
   public boolean pending;
   public boolean missing;
   public boolean ignored;
   public boolean future;
   public ValidatePattern[] ignorePatterns = new ValidatePattern[0];
   public MigrationVersion target;
   public MigrationPattern[] cherryPick;
   public MigrationVersion schema;
   public MigrationVersion baseline;
   public MigrationVersion lastResolved = MigrationVersion.EMPTY;
   public MigrationVersion lastApplied = MigrationVersion.EMPTY;
   public MigrationVersion latestBaselineMigration = MigrationVersion.EMPTY;
   public Map<String, Integer> latestRepeatableRuns = new HashMap();

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MigrationInfoContext that = (MigrationInfoContext)o;
         if (this.outOfOrder != that.outOfOrder) {
            return false;
         } else if (this.pending != that.pending) {
            return false;
         } else if (this.missing != that.missing) {
            return false;
         } else if (this.ignored != that.ignored) {
            return false;
         } else if (this.future != that.future) {
            return false;
         } else {
            if (this.target != null) {
               if (!this.target.equals(that.target)) {
                  return false;
               }
            } else if (that.target != null) {
               return false;
            }

            if (this.schema != null) {
               if (!this.schema.equals(that.schema)) {
                  return false;
               }
            } else if (that.schema != null) {
               return false;
            }

            if (this.baseline != null) {
               if (!this.baseline.equals(that.baseline)) {
                  return false;
               }
            } else if (that.baseline != null) {
               return false;
            }

            if (this.lastResolved != null) {
               if (!this.lastResolved.equals(that.lastResolved)) {
                  return false;
               }
            } else if (that.lastResolved != null) {
               return false;
            }

            if (this.lastApplied != null) {
               if (!this.lastApplied.equals(that.lastApplied)) {
                  return false;
               }
            } else if (that.lastApplied != null) {
               return false;
            }

            if (this.cherryPick != null) {
               if (!this.cherryPick.equals(that.cherryPick)) {
                  return false;
               }
            } else if (that.cherryPick != null) {
               return false;
            }

            return this.latestRepeatableRuns.equals(that.latestRepeatableRuns);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.outOfOrder ? 1 : 0;
      result = 31 * result + (this.pending ? 1 : 0);
      result = 31 * result + (this.missing ? 1 : 0);
      result = 31 * result + (this.ignored ? 1 : 0);
      result = 31 * result + (this.future ? 1 : 0);
      result = 31 * result + (this.target != null ? this.target.hashCode() : 0);
      result = 31 * result + (this.schema != null ? this.schema.hashCode() : 0);
      result = 31 * result + (this.baseline != null ? this.baseline.hashCode() : 0);
      result = 31 * result + (this.lastResolved != null ? this.lastResolved.hashCode() : 0);
      result = 31 * result + (this.lastApplied != null ? this.lastApplied.hashCode() : 0);
      result = 31 * result + (this.cherryPick != null ? this.cherryPick.hashCode() : 0);
      return 31 * result + this.latestRepeatableRuns.hashCode();
   }
}
