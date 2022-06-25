package io.micronaut.inject.ast;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.util.Collections;
import java.util.Map;

@Internal
final class SimpleClassElement implements ClassElement {
   private final String typeName;
   private final boolean isInterface;
   private final AnnotationMetadata annotationMetadata;
   private final Map<String, ClassElement> typeArguments;

   SimpleClassElement(String typeName) {
      this(typeName, false, AnnotationMetadata.EMPTY_METADATA);
   }

   SimpleClassElement(String typeName, boolean isInterface, AnnotationMetadata annotationMetadata) {
      this(typeName, isInterface, annotationMetadata, Collections.emptyMap());
   }

   SimpleClassElement(String typeName, boolean isInterface, AnnotationMetadata annotationMetadata, Map<String, ClassElement> typeArguments) {
      this.typeName = typeName;
      this.isInterface = isInterface;
      this.annotationMetadata = annotationMetadata != null ? annotationMetadata : AnnotationMetadata.EMPTY_METADATA;
      this.typeArguments = typeArguments;
   }

   @NonNull
   @Override
   public Map<String, ClassElement> getTypeArguments() {
      return this.typeArguments;
   }

   @NonNull
   @Override
   public Map<String, ClassElement> getTypeArguments(@NonNull String type) {
      return this.typeName.equals(type) ? this.typeArguments : ClassElement.super.getTypeArguments(type);
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Override
   public boolean isInterface() {
      return this.isInterface;
   }

   @Override
   public boolean isAssignable(String type) {
      return this.typeName.equals(type);
   }

   @Override
   public boolean isAssignable(ClassElement type) {
      return false;
   }

   @Override
   public ClassElement toArray() {
      throw new UnsupportedOperationException("Cannot convert class elements produced by name to an array");
   }

   @Override
   public ClassElement fromArray() {
      throw new UnsupportedOperationException("Cannot convert class elements produced by from an array");
   }

   @NonNull
   @Override
   public String getName() {
      return this.typeName;
   }

   @Override
   public boolean isPackagePrivate() {
      return false;
   }

   @Override
   public boolean isProtected() {
      return false;
   }

   @Override
   public boolean isPublic() {
      return false;
   }

   @NonNull
   @Override
   public Object getNativeType() {
      return this.typeName;
   }
}
