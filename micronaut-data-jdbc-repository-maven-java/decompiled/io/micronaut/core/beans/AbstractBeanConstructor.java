package io.micronaut.core.beans;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import java.util.Objects;

public abstract class AbstractBeanConstructor<T> implements BeanConstructor<T> {
   private final Class<T> beanType;
   private final AnnotationMetadata annotationMetadata;
   private final Argument<?>[] arguments;

   protected AbstractBeanConstructor(Class<T> beanType, AnnotationMetadata annotationMetadata, Argument<?>... arguments) {
      this.beanType = (Class)Objects.requireNonNull(beanType, "Bean type should not be null");
      this.annotationMetadata = annotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : annotationMetadata;
      this.arguments = ArrayUtils.isEmpty(arguments) ? Argument.ZERO_ARGUMENTS : arguments;
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @NonNull
   @Override
   public Class<T> getDeclaringBeanType() {
      return this.beanType;
   }

   @NonNull
   @Override
   public Argument<?>[] getArguments() {
      return this.arguments;
   }
}
