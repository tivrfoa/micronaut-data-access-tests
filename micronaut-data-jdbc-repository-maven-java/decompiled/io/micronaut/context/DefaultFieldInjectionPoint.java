package io.micronaut.context;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.annotation.AbstractEnvironmentAnnotationMetadata;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

@Internal
class DefaultFieldInjectionPoint<B, T> implements FieldInjectionPoint<B, T>, EnvironmentConfigurable {
   private final BeanDefinition declaringBean;
   private final Class declaringType;
   private final Class<T> fieldType;
   private final String field;
   private final AnnotationMetadata annotationMetadata;
   private final Argument[] typeArguments;
   private Environment environment;

   DefaultFieldInjectionPoint(
      BeanDefinition declaringBean,
      Class declaringType,
      Class<T> fieldType,
      String field,
      @Nullable AnnotationMetadata annotationMetadata,
      @Nullable Argument[] typeArguments
   ) {
      this.declaringBean = declaringBean;
      this.declaringType = declaringType;
      this.fieldType = fieldType;
      this.field = field;
      this.annotationMetadata = this.initAnnotationMetadata(annotationMetadata);
      this.typeArguments = ArrayUtils.isEmpty(typeArguments) ? Argument.ZERO_ARGUMENTS : typeArguments;
   }

   @Override
   public void configure(Environment environment) {
      this.environment = environment;
   }

   public String toString() {
      return this.fieldType.getSimpleName() + " " + this.field;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DefaultFieldInjectionPoint<?, ?> that = (DefaultFieldInjectionPoint)o;
         return Objects.equals(this.declaringType, that.declaringType)
            && Objects.equals(this.fieldType, that.fieldType)
            && Objects.equals(this.field, that.field);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.declaringType, this.fieldType, this.field});
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Override
   public String getName() {
      return this.field;
   }

   @Override
   public Field getField() {
      return ReflectionUtils.getRequiredField(this.declaringType, this.field);
   }

   @Override
   public Class<T> getType() {
      return this.fieldType;
   }

   @Override
   public void set(T instance, Object object) {
      Field field = this.getField();
      ReflectionUtils.setField(field, instance, object);
   }

   @NonNull
   @Override
   public Argument<T> asArgument() {
      return Argument.of(this.fieldType, this.field, this.annotationMetadata, this.typeArguments);
   }

   @Override
   public BeanDefinition getDeclaringBean() {
      return this.declaringBean;
   }

   @Override
   public boolean requiresReflection() {
      return false;
   }

   @Override
   public <T extends Annotation> T synthesize(Class<T> annotationClass) {
      return this.getAnnotationMetadata().synthesize(annotationClass);
   }

   @Override
   public Annotation[] synthesizeAll() {
      return this.getAnnotationMetadata().synthesizeAll();
   }

   @Override
   public Annotation[] synthesizeDeclared() {
      return this.getAnnotationMetadata().synthesizeDeclared();
   }

   private AnnotationMetadata initAnnotationMetadata(@Nullable AnnotationMetadata annotationMetadata) {
      if (annotationMetadata instanceof DefaultAnnotationMetadata) {
         return (AnnotationMetadata)(annotationMetadata.hasPropertyExpressions()
            ? new DefaultFieldInjectionPoint.FieldAnnotationMetadata((DefaultAnnotationMetadata)annotationMetadata)
            : annotationMetadata);
      } else {
         return annotationMetadata != null ? annotationMetadata : AnnotationMetadata.EMPTY_METADATA;
      }
   }

   private final class FieldAnnotationMetadata extends AbstractEnvironmentAnnotationMetadata {
      FieldAnnotationMetadata(DefaultAnnotationMetadata targetMetadata) {
         super(targetMetadata);
      }

      @Nullable
      @Override
      protected Environment getEnvironment() {
         return DefaultFieldInjectionPoint.this.environment;
      }
   }
}
