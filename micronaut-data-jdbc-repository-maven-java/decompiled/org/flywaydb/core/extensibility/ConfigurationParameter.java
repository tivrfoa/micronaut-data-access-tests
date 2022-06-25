package org.flywaydb.core.extensibility;

public class ConfigurationParameter {
   public final String name;
   public final String description;
   public final boolean required;

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public boolean isRequired() {
      return this.required;
   }

   public ConfigurationParameter(String name, String description, boolean required) {
      this.name = name;
      this.description = description;
      this.required = required;
   }
}
