package io.micronaut.context;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.MethodInjectionPoint;
import io.micronaut.inject.annotation.AbstractEnvironmentAnnotationMetadata;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

@Internal
class DefaultMethodInjectionPoint<B, T> implements MethodInjectionPoint<B, T>, EnvironmentConfigurable {
   private final BeanDefinition<B> declaringBean;
   private final AnnotationMetadata annotationMetadata;
   private final Class<?> declaringType;
   private final String methodName;
   private final Class[] argTypes;
   private final Argument<?>[] arguments;
   private Environment environment;

   DefaultMethodInjectionPoint(
      BeanDefinition<B> declaringBean,
      Class<?> declaringType,
      String methodName,
      @Nullable Argument<?>[] arguments,
      @Nullable AnnotationMetadata annotationMetadata
   ) {
      Objects.requireNonNull(declaringBean, "Declaring bean cannot be null");
      this.declaringType = declaringType;
      this.methodName = methodName;
      this.arguments = arguments == null ? Argument.ZERO_ARGUMENTS : arguments;
      this.argTypes = Argument.toClassArray(arguments);
      this.declaringBean = declaringBean;
      this.annotationMetadata = this.initAnnotationMetadata(annotationMetadata);
   }

   @Override
   public final boolean hasPropertyExpressions() {
      return this.annotationMetadata.hasPropertyExpressions();
   }

   public String toString() {
      String text = Argument.toString(this.getArguments());
      return this.declaringType.getSimpleName() + "." + this.methodName + "(" + text + ")";
   }

   @Override
   public void configure(Environment environment) {
      this.environment = environment;
   }

   @Override
   public Method getMethod() {
      Method method = (Method)ReflectionUtils.getMethod(this.declaringType, this.methodName, this.argTypes)
         .orElseThrow(() -> ReflectionUtils.newNoSuchMethodError(this.declaringType, this.methodName, this.argTypes));
      method.setAccessible(true);
      return method;
   }

   @Override
   public String getName() {
      return this.methodName;
   }

   @Override
   public boolean isPreDestroyMethod() {
      return this.annotationMetadata.hasDeclaredAnnotation("javax.annotation.PreDestroy");
   }

   @Override
   public boolean isPostConstructMethod() {
      return this.annotationMetadata.hasDeclaredAnnotation("javax.annotation.PostConstruct");
   }

   @Override
   public T invoke(Object instance, Object... args) {
      Method targetMethod = this.getMethod();
      return ReflectionUtils.invokeMethod(instance, targetMethod, args);
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @NonNull
   @Override
   public BeanDefinition<B> getDeclaringBean() {
      return this.declaringBean;
   }

   @Override
   public boolean requiresReflection() {
      return false;
   }

   @NonNull
   @Override
   public Argument<?>[] getArguments() {
      return this.arguments;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DefaultMethodInjectionPoint<B, T> that = (DefaultMethodInjectionPoint)o;
         return Objects.equals(this.declaringType, that.declaringType)
            && Objects.equals(this.methodName, that.methodName)
            && Arrays.equals(this.argTypes, that.argTypes);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = Objects.hash(new Object[]{this.declaringType, this.methodName});
      return 31 * result + Arrays.hashCode(this.argTypes);
   }

   private AnnotationMetadata initAnnotationMetadata(@Nullable AnnotationMetadata annotationMetadata) {
      if (annotationMetadata == AnnotationMetadata.EMPTY_METADATA || annotationMetadata == null) {
         return AnnotationMetadata.EMPTY_METADATA;
      } else {
         return (AnnotationMetadata)(annotationMetadata.hasPropertyExpressions()
            ? new DefaultMethodInjectionPoint.MethodAnnotationMetadata(annotationMetadata)
            : annotationMetadata);
      }
   }

   private final class MethodAnnotationMetadata extends AbstractEnvironmentAnnotationMetadata {
      MethodAnnotationMetadata(AnnotationMetadata targetMetadata) {
         super(targetMetadata);
      }

      @Nullable
      @Override
      protected Environment getEnvironment() {
         return DefaultMethodInjectionPoint.this.environment;
      }
   }
}
