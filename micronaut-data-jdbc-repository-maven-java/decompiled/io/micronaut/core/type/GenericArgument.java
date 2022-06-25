package io.micronaut.core.type;

import io.micronaut.core.annotation.AnnotationMetadata;

public abstract class GenericArgument<T> extends DefaultArgument<T> {
   protected GenericArgument() {
      super(null, null, AnnotationMetadata.EMPTY_METADATA);
   }
}
