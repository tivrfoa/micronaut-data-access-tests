package io.micronaut.inject.beans;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.AbstractBeanMethod;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;
import java.lang.reflect.Method;

@Internal
public abstract class AbstractExecutableBeanMethod<B, T> extends AbstractBeanMethod<B, T> implements ExecutableMethod<B, T> {
   protected AbstractExecutableBeanMethod(
      @NonNull BeanIntrospection<B> introspection,
      @NonNull Argument<T> returnType,
      @NonNull String name,
      @Nullable AnnotationMetadata annotationMetadata,
      @Nullable Argument<?>... arguments
   ) {
      super(introspection, returnType, name, annotationMetadata, arguments);
   }

   @Override
   public Method getTargetMethod() {
      if (ClassUtils.REFLECTION_LOGGER.isWarnEnabled()) {
         ClassUtils.REFLECTION_LOGGER
            .warn(
               "Using getTargetMethod for method {} on type {} requires the use of reflection. GraalVM configuration necessary",
               this.getName(),
               this.getDeclaringType()
            );
      }

      return ReflectionUtils.getRequiredMethod(this.getDeclaringType(), this.getMethodName(), this.getArgumentTypes());
   }

   @Override
   public Class<B> getDeclaringType() {
      return this.getDeclaringBean().getBeanType();
   }

   @Override
   public String getMethodName() {
      return this.getName();
   }
}
