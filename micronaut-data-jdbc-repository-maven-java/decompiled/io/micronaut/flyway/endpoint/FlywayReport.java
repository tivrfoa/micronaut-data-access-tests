package io.micronaut.flyway.endpoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import java.util.List;
import org.flywaydb.core.api.MigrationInfo;

@Introspected
public class FlywayReport {
   private final String name;
   private final List<MigrationInfo> migrations;

   @JsonCreator
   @Creator
   public FlywayReport(String name, List<MigrationInfo> changeSets) {
      this.name = name;
      this.migrations = changeSets;
   }

   public String getName() {
      return this.name;
   }

   @JsonSerialize(
      contentAs = MigrationInfo.class
   )
   public List<MigrationInfo> getMigrations() {
      return this.migrations;
   }
}
