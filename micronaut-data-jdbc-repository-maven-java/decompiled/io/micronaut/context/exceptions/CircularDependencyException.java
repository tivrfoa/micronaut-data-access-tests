package io.micronaut.context.exceptions;

import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.MethodInjectionPoint;

public class CircularDependencyException extends DependencyInjectionException {
   public CircularDependencyException(BeanResolutionContext resolutionContext, Argument argument, String message) {
      super(resolutionContext, argument, message, true);
   }

   public CircularDependencyException(BeanResolutionContext resolutionContext, FieldInjectionPoint fieldInjectionPoint, String message) {
      super(resolutionContext, fieldInjectionPoint, message, true);
   }

   public CircularDependencyException(BeanResolutionContext resolutionContext, BeanDefinition declaringType, String fieldName, String message) {
      super(resolutionContext, declaringType, fieldName, message, true);
   }

   public CircularDependencyException(BeanResolutionContext resolutionContext, MethodInjectionPoint methodInjectionPoint, Argument argument, String message) {
      super(resolutionContext, methodInjectionPoint, argument, message, true);
   }

   public CircularDependencyException(
      BeanResolutionContext resolutionContext, BeanDefinition declaringType, String methodName, Argument argument, String message
   ) {
      super(resolutionContext, declaringType, methodName, argument, message, true);
   }
}
