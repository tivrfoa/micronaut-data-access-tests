package org.flywaydb.core.internal.resource;

public enum ResourceType {
   MIGRATION,
   REPEATABLE_MIGRATION,
   CALLBACK;

   public static boolean isVersioned(ResourceType type) {
      return type == MIGRATION;
   }
}
