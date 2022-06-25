package io.micronaut.inject.ast.beans;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MemberElement;
import io.micronaut.inject.ast.MethodElement;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface BeanElementBuilder extends ConfigurableElement {
   @NonNull
   default BeanElementBuilder intercept(AnnotationValue<?>... annotationValue) {
      if (annotationValue != null) {
         for(AnnotationValue<?> value : annotationValue) {
            this.annotate(value);
         }
      }

      return this;
   }

   @NonNull
   Element getOriginatingElement();

   @NonNull
   ClassElement getBeanType();

   @NonNull
   default Element getProducingElement() {
      return this.getBeanType();
   }

   @NonNull
   default ClassElement getDeclaringElement() {
      return this.getBeanType();
   }

   @NonNull
   BeanElementBuilder createWith(@NonNull MethodElement element);

   @NonNull
   BeanElementBuilder typed(ClassElement... types);

   @NonNull
   BeanElementBuilder typeArguments(@NonNull ClassElement... types);

   @NonNull
   BeanElementBuilder typeArgumentsForType(@Nullable ClassElement type, @NonNull ClassElement... types);

   @NonNull
   default BeanElementBuilder scope(@NonNull AnnotationValue<?> scope) {
      Objects.requireNonNull(scope, "Scope cannot be null");
      this.annotate(scope.getAnnotationName(), builder -> builder.members(scope.getValues()));
      return this;
   }

   @NonNull
   default BeanElementBuilder scope(@NonNull String scope) {
      Objects.requireNonNull(scope, "Scope cannot be null");
      this.annotate(scope);
      return this;
   }

   @NonNull
   BeanElementBuilder withConstructor(@NonNull Consumer<BeanConstructorElement> constructorElement);

   @NonNull
   BeanElementBuilder withMethods(@NonNull ElementQuery<MethodElement> methods, @NonNull Consumer<BeanMethodElement> beanMethods);

   @NonNull
   BeanElementBuilder withFields(@NonNull ElementQuery<FieldElement> fields, @NonNull Consumer<BeanFieldElement> beanFields);

   @NonNull
   BeanElementBuilder withParameters(Consumer<BeanParameterElement[]> parameters);

   @NonNull
   default BeanElementBuilder qualifier(@Nullable String qualifier) {
      return (BeanElementBuilder)ConfigurableElement.super.qualifier(qualifier);
   }

   @NonNull
   default BeanElementBuilder qualifier(@NonNull AnnotationValue<?> qualifier) {
      return (BeanElementBuilder)ConfigurableElement.super.qualifier(qualifier);
   }

   @NonNull
   default <T extends Annotation> BeanElementBuilder annotate(@NonNull String annotationType, @NonNull Consumer<AnnotationValueBuilder<T>> consumer) {
      return (BeanElementBuilder)ConfigurableElement.super.annotate(annotationType, consumer);
   }

   default BeanElementBuilder removeAnnotation(@NonNull String annotationType) {
      return (BeanElementBuilder)ConfigurableElement.super.removeAnnotation(annotationType);
   }

   default <T extends Annotation> BeanElementBuilder removeAnnotation(@NonNull Class<T> annotationType) {
      return (BeanElementBuilder)ConfigurableElement.super.removeAnnotation(annotationType);
   }

   default <T extends Annotation> BeanElementBuilder removeAnnotationIf(@NonNull Predicate<AnnotationValue<T>> predicate) {
      return (BeanElementBuilder)ConfigurableElement.super.removeAnnotationIf(predicate);
   }

   default BeanElementBuilder removeStereotype(@NonNull String annotationType) {
      return (BeanElementBuilder)ConfigurableElement.super.removeStereotype(annotationType);
   }

   default <T extends Annotation> BeanElementBuilder removeStereotype(@NonNull Class<T> annotationType) {
      return (BeanElementBuilder)ConfigurableElement.super.removeStereotype(annotationType);
   }

   @NonNull
   default BeanElementBuilder annotate(@NonNull String annotationType) {
      return (BeanElementBuilder)ConfigurableElement.super.annotate(annotationType);
   }

   @NonNull
   default <T extends Annotation> BeanElementBuilder annotate(@NonNull Class<T> annotationType, @NonNull Consumer<AnnotationValueBuilder<T>> consumer) {
      return (BeanElementBuilder)ConfigurableElement.super.annotate(annotationType, consumer);
   }

   @NonNull
   default <T extends Annotation> BeanElementBuilder annotate(@NonNull Class<T> annotationType) {
      return (BeanElementBuilder)ConfigurableElement.super.annotate(annotationType);
   }

   BeanElementBuilder inject();

   <E extends MemberElement> BeanElementBuilder produceBeans(ElementQuery<E> methodsOrFields, @Nullable Consumer<BeanElementBuilder> childBeanBuilder);

   default <E extends MemberElement> BeanElementBuilder produceBeans(ElementQuery<E> methodsOrFields) {
      return this.produceBeans(methodsOrFields, null);
   }
}
