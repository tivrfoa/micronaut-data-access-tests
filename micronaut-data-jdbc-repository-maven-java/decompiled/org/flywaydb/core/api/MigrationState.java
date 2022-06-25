package org.flywaydb.core.api;

public enum MigrationState {
   PENDING("Pending", true, false, false),
   ABOVE_TARGET("Above Target", true, false, false),
   BELOW_BASELINE("Below Baseline", true, false, false),
   BASELINE("Baseline", true, true, false),
   IGNORED("Ignored", true, false, false),
   MISSING_SUCCESS("Missing", false, true, false),
   MISSING_FAILED("Failed (Missing)", false, true, true),
   SUCCESS("Success", true, true, false),
   UNDONE("Undone", true, true, false),
   AVAILABLE("Available", true, false, false),
   FAILED("Failed", true, true, true),
   OUT_OF_ORDER("Out of Order", true, true, false),
   FUTURE_SUCCESS("Future", false, true, false),
   FUTURE_FAILED("Failed (Future)", false, true, true),
   OUTDATED("Outdated", true, true, false),
   SUPERSEDED("Superseded", true, true, false),
   DELETED("Deleted", false, true, false);

   private final String displayName;
   private final boolean resolved;
   private final boolean applied;
   private final boolean failed;

   private MigrationState(String displayName, boolean resolved, boolean applied, boolean failed) {
      this.displayName = displayName;
      this.resolved = resolved;
      this.applied = applied;
      this.failed = failed;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public boolean isResolved() {
      return this.resolved;
   }

   public boolean isApplied() {
      return this.applied;
   }

   public boolean isFailed() {
      return this.failed;
   }
}
