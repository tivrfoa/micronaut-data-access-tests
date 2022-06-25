package io.micronaut.inject.configuration;

import io.micronaut.inject.writer.ClassWriterOutputVisitor;
import java.io.IOException;

public interface ConfigurationMetadataWriter {
   void write(ConfigurationMetadataBuilder<?> metadataBuilder, ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException;
}
