package io.micronaut.inject.ast;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.annotation.MutableAnnotationMetadata;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;

final class ReflectParameterElement implements ParameterElement {
   private final ClassElement classElement;
   private final String name;
   private AnnotationMetadata annotationMetadata = AnnotationMetadata.EMPTY_METADATA;

   ReflectParameterElement(ClassElement classElement, String name) {
      this.classElement = classElement;
      this.name = name;
   }

   @Override
   public boolean isPrimitive() {
      return this.classElement.isPrimitive();
   }

   @Override
   public boolean isArray() {
      return this.classElement.isArray();
   }

   @Override
   public int getArrayDimensions() {
      return this.classElement.getArrayDimensions();
   }

   @NonNull
   @Override
   public ClassElement getType() {
      return this.classElement;
   }

   @NonNull
   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public boolean isProtected() {
      return false;
   }

   @Override
   public boolean isPublic() {
      return true;
   }

   @NonNull
   @Override
   public Object getNativeType() {
      return this.classElement.getNativeType();
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @NonNull
   @Override
   public <T extends Annotation> Element annotate(@NonNull String annotationType, @NonNull Consumer<AnnotationValueBuilder<T>> consumer) {
      if (this.annotationMetadata == AnnotationMetadata.EMPTY_METADATA) {
         MutableAnnotationMetadata mutableAnnotationMetadata = new MutableAnnotationMetadata();
         this.annotationMetadata = mutableAnnotationMetadata;
         AnnotationValueBuilder<T> builder = AnnotationValue.builder(annotationType);
         consumer.accept(builder);
         mutableAnnotationMetadata.addDeclaredAnnotation(annotationType, builder.build().getValues());
      } else {
         AnnotationValueBuilder<T> builder = AnnotationValue.builder(annotationType);
         consumer.accept(builder);
         this.annotationMetadata = DefaultAnnotationMetadata.mutateMember(this.annotationMetadata, annotationType, builder.build().getValues());
      }

      return this;
   }
}
