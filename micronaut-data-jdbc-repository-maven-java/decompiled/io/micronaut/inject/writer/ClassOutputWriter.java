package io.micronaut.inject.writer;

import java.io.IOException;

public interface ClassOutputWriter {
   void accept(ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException;
}
