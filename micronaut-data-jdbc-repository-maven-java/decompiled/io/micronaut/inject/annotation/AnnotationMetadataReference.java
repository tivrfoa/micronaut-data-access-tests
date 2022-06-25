package io.micronaut.inject.annotation;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataDelegate;

public class AnnotationMetadataReference implements AnnotationMetadataDelegate {
   private final String className;
   private final AnnotationMetadata annotationMetadata;

   public AnnotationMetadataReference(String className, AnnotationMetadata annotationMetadata) {
      this.className = className;
      this.annotationMetadata = annotationMetadata;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   public String getClassName() {
      return this.className;
   }
}
