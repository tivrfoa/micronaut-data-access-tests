package io.micronaut.inject.configuration;

import io.micronaut.core.io.Writable;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

public class ConfigurationMetadata implements Writable {
   String type;
   String name;
   String description;
   Set<String> includes;
   Set<String> excludes;

   public String getType() {
      return this.type;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public Set<String> getIncludes() {
      return this.includes;
   }

   public Set<String> getExcludes() {
      return this.excludes;
   }

   @Override
   public void writeTo(Writer out) throws IOException {
      out.write(123);
      ConfigurationMetadataBuilder.writeAttribute(out, "name", this.name);
      out.write(44);
      ConfigurationMetadataBuilder.writeAttribute(out, "type", this.type);
      if (this.description != null) {
         out.write(44);
         ConfigurationMetadataBuilder.writeAttribute(out, "description", this.description);
      }

      out.write(125);
   }

   public String toString() {
      return "ConfigurationMetadata{type='" + this.type + '\'' + ", name='" + this.name + '\'' + ", description='" + this.description + '\'' + '}';
   }
}
