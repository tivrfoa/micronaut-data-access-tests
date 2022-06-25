package io.micronaut.inject.writer;

import io.micronaut.asm.Type;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.configuration.ConfigurationMetadataBuilder;
import io.micronaut.inject.processing.JavaModelUtils;

@Internal
class ConfigBuilderState {
   private final String name;
   private final Type type;
   private final boolean invokeMethod;
   private final ConfigurationMetadataBuilder metadataBuilder;
   private final AnnotationMetadata annotationMetadata;
   private final boolean isInterface;

   ConfigBuilderState(
      ClassElement type,
      String name,
      boolean isMethod,
      AnnotationMetadata annotationMetadata,
      ConfigurationMetadataBuilder metadataBuilder,
      boolean isInterface
   ) {
      this.type = JavaModelUtils.getTypeReference(type);
      this.name = name;
      this.invokeMethod = isMethod;
      this.metadataBuilder = metadataBuilder;
      this.annotationMetadata = annotationMetadata;
      this.isInterface = isInterface;
   }

   public ConfigurationMetadataBuilder<?> getMetadataBuilder() {
      return this.metadataBuilder;
   }

   public String getName() {
      return this.name;
   }

   public Type getType() {
      return this.type;
   }

   public boolean isMethod() {
      return this.invokeMethod;
   }

   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   public boolean isInterface() {
      return this.isInterface;
   }
}
