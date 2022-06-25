package io.micronaut.inject.visitor;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.ast.ElementFactory;
import io.micronaut.inject.writer.ClassWriterOutputVisitor;
import io.micronaut.inject.writer.GeneratedFile;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public interface VisitorContext extends MutableConvertibleValues<Object>, ClassWriterOutputVisitor {
   String MICRONAUT_BASE_OPTION_NAME = "micronaut";
   String MICRONAUT_PROCESSING_PROJECT_DIR = "micronaut.processing.project.dir";
   String MICRONAUT_PROCESSING_GROUP = "micronaut.processing.group";
   String MICRONAUT_PROCESSING_MODULE = "micronaut.processing.module";

   @NonNull
   ElementFactory<?, ?, ?, ?> getElementFactory();

   void info(String message, @Nullable Element element);

   void info(String message);

   void fail(String message, @Nullable Element element);

   void warn(String message, @Nullable Element element);

   @NonNull
   default VisitorConfiguration getConfiguration() {
      return VisitorConfiguration.DEFAULT;
   }

   @Deprecated
   @Override
   default Optional<GeneratedFile> visitMetaInfFile(String path) {
      return this.visitMetaInfFile(path, Element.EMPTY_ELEMENT_ARRAY);
   }

   @Override
   Optional<GeneratedFile> visitMetaInfFile(String path, Element... originatingElements);

   @Override
   Optional<GeneratedFile> visitGeneratedFile(String path);

   @NonNull
   default Iterable<URL> getClasspathResources(@NonNull String path) {
      return Collections.emptyList();
   }

   default Optional<Path> getProjectDir() {
      Optional<Path> projectDir = this.get("micronaut.processing.project.dir", Path.class);
      if (projectDir.isPresent()) {
         return projectDir;
      } else {
         Optional<GeneratedFile> dummyFile = this.visitGeneratedFile("dummy");
         if (dummyFile.isPresent()) {
            URI uri = ((GeneratedFile)dummyFile.get()).toURI();
            if (uri.getScheme() != null && !uri.getScheme().equals("mem")) {
               for(Path dummy = Paths.get(uri).normalize(); dummy != null; dummy = dummy.getParent()) {
                  Path dummyFileName = dummy.getFileName();
                  if (dummyFileName != null && ("build".equals(dummyFileName.toString()) || "target".equals(dummyFileName.toString()))) {
                     projectDir = Optional.ofNullable(dummy.getParent());
                     this.put("micronaut.processing.project.dir", dummy.getParent());
                     break;
                  }
               }
            }
         }

         return projectDir;
      }
   }

   default Optional<Path> getClassesOutputPath() {
      Optional<GeneratedFile> dummy = this.visitMetaInfFile("dummy", Element.EMPTY_ELEMENT_ARRAY);
      if (dummy.isPresent()) {
         Path classesOutputDir = Paths.get(((GeneratedFile)dummy.get()).toURI()).getParent().getParent();
         return Optional.of(classesOutputDir);
      } else {
         return Optional.empty();
      }
   }

   default Optional<ClassElement> getClassElement(String name) {
      return Optional.empty();
   }

   default Optional<ClassElement> getClassElement(Class<?> type) {
      return type != null ? this.getClassElement(type.getName()) : Optional.empty();
   }

   @NonNull
   default ClassElement[] getClassElements(@NonNull String aPackage, @NonNull String... stereotypes) {
      return new ClassElement[0];
   }

   default Map<String, String> getOptions() {
      return Collections.emptyMap();
   }

   default Collection<String> getGeneratedResources() {
      this.info("EXPERIMENTAL: Compile time resource contribution to the context is experimental", null);
      return Collections.emptyList();
   }

   default void addGeneratedResource(String resource) {
      this.info("EXPERIMENTAL: Compile time resource contribution to the context is experimental", null);
   }
}
