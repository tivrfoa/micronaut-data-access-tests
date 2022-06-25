package org.flywaydb.core.api;

public interface MigrationInfoService extends InfoOutputProvider {
   MigrationInfo[] all();

   MigrationInfo current();

   MigrationInfo[] pending();

   MigrationInfo[] applied();
}
