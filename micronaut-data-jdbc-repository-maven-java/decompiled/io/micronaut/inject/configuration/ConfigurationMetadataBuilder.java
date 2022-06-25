package io.micronaut.inject.configuration;

import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.ast.Element;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class ConfigurationMetadataBuilder<T> {
   private static ConfigurationMetadataBuilder<?> currentBuilder = null;
   private final List<PropertyMetadata> properties = new ArrayList();
   private final List<ConfigurationMetadata> configurations = new ArrayList();

   @NonNull
   public abstract Element[] getOriginatingElements();

   public List<PropertyMetadata> getProperties() {
      return Collections.unmodifiableList(this.properties);
   }

   public List<ConfigurationMetadata> getConfigurations() {
      return Collections.unmodifiableList(this.configurations);
   }

   public boolean hasMetadata() {
      return !this.properties.isEmpty() || !this.configurations.isEmpty();
   }

   public ConfigurationMetadata visitProperties(T type, @Nullable String description) {
      AnnotationMetadata annotationMetadata = this.getAnnotationMetadata(type);
      return this.visitProperties(type, description, annotationMetadata);
   }

   public ConfigurationMetadata visitProperties(T type, @Nullable String description, @NonNull AnnotationMetadata annotationMetadata) {
      String path = this.buildTypePath(type, type, annotationMetadata);
      ConfigurationMetadata configurationMetadata = new ConfigurationMetadata();
      configurationMetadata.name = NameUtils.hyphenate(path, true);
      configurationMetadata.type = this.getTypeString(type);
      configurationMetadata.description = description;
      configurationMetadata.includes = CollectionUtils.setOf((T[])annotationMetadata.stringValues(ConfigurationReader.class, "includes"));
      configurationMetadata.excludes = CollectionUtils.setOf((T[])annotationMetadata.stringValues(ConfigurationReader.class, "excludes"));
      this.configurations.add(configurationMetadata);
      return configurationMetadata;
   }

   public PropertyMetadata visitProperty(
      T owningType, T declaringType, String propertyType, String name, @Nullable String description, @Nullable String defaultValue
   ) {
      PropertyMetadata metadata = new PropertyMetadata();
      metadata.declaringType = this.getTypeString(declaringType);
      metadata.name = name;
      metadata.path = NameUtils.hyphenate(this.buildPropertyPath(owningType, declaringType, name), true);
      metadata.type = propertyType;
      metadata.description = description;
      metadata.defaultValue = defaultValue;
      this.properties.add(metadata);
      return metadata;
   }

   public PropertyMetadata visitProperty(String propertyType, String name, @Nullable String description, @Nullable String defaultValue) {
      if (!this.configurations.isEmpty()) {
         ConfigurationMetadata last = (ConfigurationMetadata)this.configurations.get(this.configurations.size() - 1);
         PropertyMetadata metadata = new PropertyMetadata();
         metadata.declaringType = last.type;
         metadata.name = name;
         metadata.path = NameUtils.hyphenate(last.name + "." + name, true);
         metadata.type = propertyType;
         metadata.description = description;
         metadata.defaultValue = defaultValue;
         this.properties.add(metadata);
         return metadata;
      } else {
         return null;
      }
   }

   protected abstract String buildPropertyPath(T owningType, T declaringType, String propertyName);

   protected abstract String buildTypePath(T owningType, T declaringType);

   protected abstract String buildTypePath(T owningType, T declaringType, AnnotationMetadata annotationMetadata);

   protected abstract String getTypeString(T type);

   protected abstract AnnotationMetadata getAnnotationMetadata(T type);

   public static Optional<ConfigurationMetadataBuilder<?>> getConfigurationMetadataBuilder() {
      return Optional.ofNullable(currentBuilder);
   }

   public static void setConfigurationMetadataBuilder(@Nullable ConfigurationMetadataBuilder<?> builder) {
      currentBuilder = builder;
   }

   static String quote(String string) {
      if (string != null && string.length() != 0) {
         char c = '\u0000';
         int len = string.length();
         StringBuilder sb = new StringBuilder(len + 4);
         sb.append('"');

         for(int i = 0; i < len; ++i) {
            c = string.charAt(i);
            switch(c) {
               case '\b':
                  sb.append("\\b");
                  break;
               case '\t':
                  sb.append("\\t");
                  break;
               case '\n':
                  sb.append("\\n");
                  break;
               case '\f':
                  sb.append("\\f");
                  break;
               case '\r':
                  sb.append("\\r");
                  break;
               case '"':
               case '\\':
                  sb.append('\\');
                  sb.append(c);
                  break;
               case '/':
                  sb.append('\\');
                  sb.append(c);
                  break;
               default:
                  if (c < ' ') {
                     String t = "000" + Integer.toHexString(c);
                     sb.append("\\u").append(t.substring(t.length() - 4));
                  } else {
                     sb.append(c);
                  }
            }
         }

         sb.append('"');
         return sb.toString();
      } else {
         return "\"\"";
      }
   }

   static void writeAttribute(Writer out, String name, String value) throws IOException {
      out.write(34);
      out.write(name);
      out.write("\":");
      out.write(quote(value));
   }
}
