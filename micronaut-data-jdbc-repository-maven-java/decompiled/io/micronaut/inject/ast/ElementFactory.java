package io.micronaut.inject.ast;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import java.util.Map;

public interface ElementFactory<E, C extends E, M extends E, F extends E> {
   @NonNull
   ClassElement newClassElement(@NonNull C type, @NonNull AnnotationMetadata annotationMetadata);

   @NonNull
   ClassElement newClassElement(@NonNull C type, @NonNull AnnotationMetadata annotationMetadata, @NonNull Map<String, ClassElement> resolvedGenerics);

   @NonNull
   ClassElement newSourceClassElement(@NonNull C type, @NonNull AnnotationMetadata annotationMetadata);

   @NonNull
   MethodElement newSourceMethodElement(ClassElement declaringClass, @NonNull M method, @NonNull AnnotationMetadata annotationMetadata);

   @NonNull
   MethodElement newMethodElement(ClassElement declaringClass, @NonNull M method, @NonNull AnnotationMetadata annotationMetadata);

   @NonNull
   ConstructorElement newConstructorElement(ClassElement declaringClass, @NonNull M constructor, @NonNull AnnotationMetadata annotationMetadata);

   @NonNull
   FieldElement newFieldElement(ClassElement declaringClass, @NonNull F field, @NonNull AnnotationMetadata annotationMetadata);

   @NonNull
   FieldElement newFieldElement(@NonNull F field, @NonNull AnnotationMetadata annotationMetadata);
}
