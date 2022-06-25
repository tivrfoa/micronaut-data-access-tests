package io.micronaut.inject.writer;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.ast.Element;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ClassWriterOutputVisitor {
   default OutputStream visitClass(String classname) throws IOException {
      return this.visitClass(classname, Element.EMPTY_ELEMENT_ARRAY);
   }

   default OutputStream visitClass(String classname, @Nullable Element originatingElement) throws IOException {
      return this.visitClass(classname, originatingElement);
   }

   OutputStream visitClass(String classname, Element... originatingElements) throws IOException;

   void visitServiceDescriptor(String type, String classname);

   void visitServiceDescriptor(String type, String classname, Element originatingElement);

   @Deprecated
   default Optional<GeneratedFile> visitMetaInfFile(String path) {
      return this.visitMetaInfFile(path, Element.EMPTY_ELEMENT_ARRAY);
   }

   Optional<GeneratedFile> visitMetaInfFile(String path, Element... originatingElements);

   Optional<GeneratedFile> visitGeneratedFile(String path);

   void finish();

   default Map<String, Set<String>> getServiceEntries() {
      return Collections.emptyMap();
   }

   @Deprecated
   default void visitServiceDescriptor(Class<?> type, String classname) {
      this.visitServiceDescriptor(type.getName(), classname);
   }

   default void visitServiceDescriptor(Class<?> type, String classname, Element originatingElement) {
      this.visitServiceDescriptor(type.getName(), classname, originatingElement);
   }
}
