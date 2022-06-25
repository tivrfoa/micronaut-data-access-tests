package io.micronaut.inject.configuration;

import io.micronaut.core.io.Writable;
import java.io.IOException;
import java.io.Writer;

public class PropertyMetadata implements Writable {
   String type;
   String name;
   String description;
   String path;
   String defaultValue;
   String declaringType;

   public String getType() {
      return this.type;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getPath() {
      return this.path;
   }

   public String getDefaultValue() {
      return this.defaultValue;
   }

   public String getDeclaringType() {
      return this.declaringType;
   }

   @Override
   public void writeTo(Writer out) throws IOException {
      out.write(123);
      ConfigurationMetadataBuilder.writeAttribute(out, "name", this.path);
      out.write(44);
      ConfigurationMetadataBuilder.writeAttribute(out, "type", this.type);
      out.write(44);
      ConfigurationMetadataBuilder.writeAttribute(out, "sourceType", this.declaringType);
      if (this.description != null) {
         out.write(44);
         ConfigurationMetadataBuilder.writeAttribute(out, "description", this.description);
      }

      if (this.defaultValue != null) {
         out.write(44);
         ConfigurationMetadataBuilder.writeAttribute(out, "defaultValue", this.defaultValue);
      }

      out.write(125);
   }

   public String toString() {
      return "PropertyMetadata{type='"
         + this.type
         + '\''
         + ", name='"
         + this.name
         + '\''
         + ", description='"
         + this.description
         + '\''
         + ", path='"
         + this.path
         + '\''
         + ", defaultValue='"
         + this.defaultValue
         + '\''
         + ", declaringType='"
         + this.declaringType
         + '\''
         + '}';
   }
}
