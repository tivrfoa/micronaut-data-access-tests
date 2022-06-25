package io.micronaut.runtime;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.naming.NameUtils;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@ConfigurationProperties("micronaut.application")
@Primary
@BootstrapContextCompatible
public class ApplicationConfiguration {
   public static final String PREFIX = "micronaut.application";
   public static final String DEFAULT_CHARSET = "micronaut.application.default-charset";
   public static final String APPLICATION_NAME = "micronaut.application.name";
   private Charset defaultCharset = StandardCharsets.UTF_8;
   private String name;
   private ApplicationConfiguration.InstanceConfiguration instance = new ApplicationConfiguration.InstanceConfiguration();

   public Charset getDefaultCharset() {
      return this.defaultCharset;
   }

   public void setDefaultCharset(Charset defaultCharset) {
      this.defaultCharset = defaultCharset;
   }

   public Optional<String> getName() {
      return Optional.ofNullable(this.name);
   }

   public void setName(String name) {
      if (name != null) {
         this.name = NameUtils.hyphenate(name);
      }

   }

   public ApplicationConfiguration.InstanceConfiguration getInstance() {
      return this.instance;
   }

   public void setInstance(ApplicationConfiguration.InstanceConfiguration instance) {
      if (instance != null) {
         this.instance = instance;
      }

   }

   @ConfigurationProperties("instance")
   @BootstrapContextCompatible
   public static class InstanceConfiguration {
      public static final String PREFIX = "instance";
      public static final String INSTANCE_ID = "micronaut.application.instance.id";
      private String id;
      private String group;
      private String zone;
      private Map<String, String> metadata = Collections.emptyMap();

      public Optional<String> getId() {
         return Optional.ofNullable(this.id);
      }

      public void setId(String id) {
         this.id = id;
      }

      public Map<String, String> getMetadata() {
         return this.metadata;
      }

      public void setMetadata(Map<String, String> metadata) {
         this.metadata = metadata;
      }

      public Optional<String> getGroup() {
         return Optional.ofNullable(this.group);
      }

      public void setGroup(String group) {
         this.group = group;
      }

      public Optional<String> getZone() {
         return this.zone != null ? Optional.of(this.zone) : Optional.ofNullable(this.getMetadata().get("zone"));
      }

      public void setZone(String zone) {
         this.zone = zone;
      }
   }
}
