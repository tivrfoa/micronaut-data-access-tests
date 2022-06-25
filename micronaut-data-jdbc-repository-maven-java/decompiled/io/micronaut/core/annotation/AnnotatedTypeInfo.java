package io.micronaut.core.annotation;

public interface AnnotatedTypeInfo {
   boolean isAbstract();

   String getTypeName();

   boolean hasAnnotation(String annotationName);
}
