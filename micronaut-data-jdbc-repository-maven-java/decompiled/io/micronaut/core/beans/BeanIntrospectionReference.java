package io.micronaut.core.beans;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;

@Internal
public interface BeanIntrospectionReference<T> extends AnnotationMetadataProvider, Named {
   boolean isPresent();

   @NonNull
   Class<T> getBeanType();

   @NonNull
   BeanIntrospection<T> load();
}
