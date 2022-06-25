package io.micronaut.flyway;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.FluentConfiguration;

@Context
@EachProperty("flyway.datasources")
public class FlywayConfigurationProperties implements Toggleable {
   public static final boolean DEFAULT_ENABLED = true;
   public static final boolean DEFAULT_ASYNC = false;
   public static final boolean DEFAULT_CLEAN_SCHEMA = false;
   @ConfigurationBuilder(
      prefixes = {""},
      excludes = {"locations", "jdbcProperties"}
   )
   FluentConfiguration fluentConfiguration = new FluentConfiguration();
   private final String nameQualifier;
   private boolean enabled = true;
   private boolean async = false;
   private boolean cleanSchema = false;
   private String url;
   private String user;
   private String password;

   public FlywayConfigurationProperties(@Parameter String name) {
      this.nameQualifier = name;
   }

   public String getNameQualifier() {
      return this.nameQualifier;
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isAsync() {
      return this.async;
   }

   public void setAsync(boolean async) {
      this.async = async;
   }

   public boolean isCleanSchema() {
      return this.cleanSchema;
   }

   public void setCleanSchema(boolean cleanSchema) {
      this.cleanSchema = cleanSchema;
   }

   public String getUrl() {
      return this.url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String getUser() {
      return this.user;
   }

   public void setUser(String user) {
      this.user = user;
   }

   public void setUsername(String username) {
      this.user = username;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public Location[] getLocations() {
      return this.fluentConfiguration.getLocations();
   }

   public void setLocations(String... locations) {
      this.fluentConfiguration.locations(locations);
   }

   public boolean hasAlternativeDatabaseConfiguration() {
      return StringUtils.hasText(this.getUrl());
   }

   public FluentConfiguration getFluentConfiguration() {
      return this.fluentConfiguration;
   }
}
