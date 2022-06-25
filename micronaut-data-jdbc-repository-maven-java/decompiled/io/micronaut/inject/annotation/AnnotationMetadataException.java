package io.micronaut.inject.annotation;

import io.micronaut.context.exceptions.BeanContextException;

public class AnnotationMetadataException extends BeanContextException {
   public AnnotationMetadataException(String message, Throwable cause) {
      super(message, cause);
   }

   public AnnotationMetadataException(String message) {
      super(message);
   }
}
