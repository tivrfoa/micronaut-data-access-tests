package org.flywaydb.core.internal.license;

public enum Edition {
   COMMUNITY("Community"),
   PRO("Teams"),
   ENTERPRISE("Teams"),
   TIER3("Enterprise");

   private final String description;

   private Edition(String name) {
      this.description = "Flyway " + name + " Edition";
   }

   public String toString() {
      return this.description;
   }

   public String getDescription() {
      return this.description;
   }
}
