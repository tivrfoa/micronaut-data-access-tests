package io.micronaut.inject.configuration;

import io.micronaut.core.io.Writable;
import io.micronaut.inject.writer.ClassWriterOutputVisitor;
import io.micronaut.inject.writer.GeneratedFile;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class JsonConfigurationMetadataWriter implements ConfigurationMetadataWriter {
   @Override
   public void write(ConfigurationMetadataBuilder<?> metadataBuilder, ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {
      Optional<GeneratedFile> opt = classWriterOutputVisitor.visitMetaInfFile(this.getFileName(), metadataBuilder.getOriginatingElements());
      if (opt.isPresent()) {
         GeneratedFile file = (GeneratedFile)opt.get();
         List<ConfigurationMetadata> configurations = metadataBuilder.getConfigurations();
         List<PropertyMetadata> properties = metadataBuilder.getProperties();
         Writer writer = file.openWriter();
         Throwable var8 = null;

         try {
            writer.write(123);
            boolean hasGroups = !configurations.isEmpty();
            boolean hasProps = !properties.isEmpty();
            if (hasGroups) {
               this.writeMetadata("groups", configurations, writer);
               if (hasProps) {
                  writer.write(44);
               }
            }

            if (hasProps) {
               this.writeMetadata("properties", properties, writer);
            }

            writer.write(125);
         } catch (Throwable var18) {
            var8 = var18;
            throw var18;
         } finally {
            if (writer != null) {
               if (var8 != null) {
                  try {
                     writer.close();
                  } catch (Throwable var17) {
                     var8.addSuppressed(var17);
                  }
               } else {
                  writer.close();
               }
            }

         }
      }

   }

   protected String getFileName() {
      return "spring-configuration-metadata.json";
   }

   private void writeMetadata(String attr, List<? extends Writable> configurations, Writer writer) throws IOException {
      writer.write(34);
      writer.write(attr);
      writer.write("\":[");
      Iterator<? extends Writable> i = configurations.iterator();

      while(i.hasNext()) {
         Writable metadata = (Writable)i.next();
         metadata.writeTo(writer);
         if (i.hasNext()) {
            writer.write(44);
         }
      }

      writer.write(93);
   }
}
