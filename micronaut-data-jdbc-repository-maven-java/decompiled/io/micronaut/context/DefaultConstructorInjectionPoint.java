package io.micronaut.context;

import io.micronaut.context.env.Environment;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ConstructorInjectionPoint;
import io.micronaut.inject.annotation.AbstractEnvironmentAnnotationMetadata;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Internal
class DefaultConstructorInjectionPoint<T> implements ConstructorInjectionPoint<T>, EnvironmentConfigurable {
   private final BeanDefinition<T> declaringBean;
   private final Class<T> declaringType;
   private final Class[] argTypes;
   private final AnnotationMetadata annotationMetadata;
   private final Argument<?>[] arguments;
   private Environment environment;

   DefaultConstructorInjectionPoint(BeanDefinition<T> declaringBean, Class<T> declaringType, AnnotationMetadata annotationMetadata, Argument<?>[] arguments) {
      this.argTypes = Argument.toClassArray(arguments);
      this.declaringBean = declaringBean;
      this.declaringType = declaringType;
      if (!(annotationMetadata instanceof DefaultAnnotationMetadata)) {
         this.annotationMetadata = AnnotationMetadata.EMPTY_METADATA;
      } else if (annotationMetadata.hasPropertyExpressions()) {
         this.annotationMetadata = new DefaultConstructorInjectionPoint.ConstructorAnnotationMetadata((DefaultAnnotationMetadata)annotationMetadata);
      } else {
         this.annotationMetadata = annotationMetadata;
      }

      this.arguments = arguments == null ? Argument.ZERO_ARGUMENTS : arguments;
   }

   @Override
   public final boolean hasPropertyExpressions() {
      return this.annotationMetadata.hasPropertyExpressions();
   }

   @Override
   public void configure(Environment environment) {
      this.environment = environment;
   }

   @Override
   public T invoke(Object... args) {
      Optional<Constructor<T>> potentialConstructor = ReflectionUtils.findConstructor(this.declaringType, this.argTypes);
      if (potentialConstructor.isPresent()) {
         return ReflectionConstructorInjectionPoint.invokeConstructor((Constructor<T>)potentialConstructor.get(), this.arguments, args);
      } else {
         throw new BeanInstantiationException("Constructor not found for type: " + this);
      }
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @NonNull
   @Override
   public Argument<?>[] getArguments() {
      return this.arguments;
   }

   @NonNull
   @Override
   public BeanDefinition<T> getDeclaringBean() {
      return this.declaringBean;
   }

   @Override
   public final boolean requiresReflection() {
      return false;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DefaultConstructorInjectionPoint<?> that = (DefaultConstructorInjectionPoint)o;
         return Objects.equals(this.declaringType, that.declaringType) && Arrays.equals(this.argTypes, that.argTypes);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = Objects.hash(new Object[]{this.declaringType});
      return 31 * result + Arrays.hashCode(this.argTypes);
   }

   public String toString() {
      return this.declaringType.getName() + "(" + Argument.toString(this.arguments) + ")";
   }

   private final class ConstructorAnnotationMetadata extends AbstractEnvironmentAnnotationMetadata {
      ConstructorAnnotationMetadata(DefaultAnnotationMetadata targetMetadata) {
         super(targetMetadata);
      }

      @Nullable
      @Override
      protected Environment getEnvironment() {
         return DefaultConstructorInjectionPoint.this.environment;
      }
   }
}
