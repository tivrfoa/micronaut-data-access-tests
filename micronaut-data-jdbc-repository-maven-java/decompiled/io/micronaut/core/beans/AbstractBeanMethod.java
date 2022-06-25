package io.micronaut.core.beans;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import java.util.Map;

@Internal
public abstract class AbstractBeanMethod<B, T> implements BeanMethod<B, T> {
   private final String name;
   private final AnnotationMetadata annotationMetadata;
   private final Argument<?>[] arguments;
   private final Argument<T> returnType;
   private final BeanIntrospection<B> introspection;

   protected AbstractBeanMethod(
      @NonNull BeanIntrospection<B> introspection,
      @NonNull Argument<T> returnType,
      @NonNull String name,
      @Nullable AnnotationMetadata annotationMetadata,
      @Nullable Argument<?>... arguments
   ) {
      this.introspection = introspection;
      this.name = name;
      this.annotationMetadata = annotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : annotationMetadata;
      this.arguments = arguments == null ? Argument.ZERO_ARGUMENTS : arguments;
      this.returnType = returnType;
   }

   @NonNull
   @Override
   public BeanIntrospection<B> getDeclaringBean() {
      return this.introspection;
   }

   @NonNull
   @Override
   public final ReturnType<T> getReturnType() {
      return new ReturnType() {
         @Override
         public Class<T> getType() {
            return AbstractBeanMethod.this.returnType.getType();
         }

         @NonNull
         @Override
         public Argument<T> asArgument() {
            return AbstractBeanMethod.this.returnType;
         }

         @Override
         public Map<String, Argument<?>> getTypeVariables() {
            return AbstractBeanMethod.this.returnType.getTypeVariables();
         }

         @NonNull
         @Override
         public AnnotationMetadata getAnnotationMetadata() {
            return AbstractBeanMethod.this.returnType.getAnnotationMetadata();
         }
      };
   }

   @NonNull
   @Override
   public final AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @NonNull
   @Override
   public final String getName() {
      return this.name;
   }

   @Override
   public final Argument<?>[] getArguments() {
      return this.arguments;
   }

   @Override
   public T invoke(@NonNull B instance, Object... arguments) {
      return this.invokeInternal(instance, arguments);
   }

   @Internal
   protected abstract T invokeInternal(B instance, Object... arguments);
}
