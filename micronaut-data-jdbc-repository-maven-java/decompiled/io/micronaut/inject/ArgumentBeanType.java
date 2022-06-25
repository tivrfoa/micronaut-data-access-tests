package io.micronaut.inject;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import java.util.Map;

public final class ArgumentBeanType<T> implements BeanType<T>, Argument<T> {
   private final Argument<T> argument;

   public ArgumentBeanType(@NonNull Argument<T> argument) {
      ArgumentUtils.requireNonNull("argument", argument);
      this.argument = argument;
   }

   @Override
   public boolean isContainerType() {
      return BeanType.super.isContainerType();
   }

   @Override
   public String getName() {
      return this.argument.getName();
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.argument.getAnnotationMetadata();
   }

   @Override
   public Map<String, Argument<?>> getTypeVariables() {
      return this.argument.getTypeVariables();
   }

   @Override
   public Class<T> getType() {
      return this.argument.getType();
   }

   @Override
   public boolean equalsType(@Nullable Argument<?> other) {
      return this.argument.equals(other);
   }

   @Override
   public int typeHashCode() {
      return this.argument.getType().hashCode();
   }

   @Override
   public boolean isPrimary() {
      return true;
   }

   @Override
   public Class<T> getBeanType() {
      return this.argument.getType();
   }

   @Override
   public boolean isEnabled(BeanContext context) {
      return true;
   }

   @Override
   public boolean isEnabled(@NonNull BeanContext context, @Nullable BeanResolutionContext resolutionContext) {
      return true;
   }
}
