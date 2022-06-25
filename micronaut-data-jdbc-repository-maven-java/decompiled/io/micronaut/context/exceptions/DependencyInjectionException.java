package io.micronaut.context.exceptions;

import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.MethodInjectionPoint;
import java.util.Optional;

public class DependencyInjectionException extends BeanCreationException {
   public DependencyInjectionException(BeanResolutionContext resolutionContext, Throwable cause) {
      super(
         resolutionContext,
         MessageUtils.buildMessage(resolutionContext, !(cause instanceof BeanInstantiationException) ? cause.getMessage() : null, false),
         cause
      );
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, Argument argument, Throwable cause) {
      super(
         resolutionContext,
         MessageUtils.buildMessage(resolutionContext, argument, !(cause instanceof BeanInstantiationException) ? cause.getMessage() : null, false),
         cause
      );
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, String message, Throwable cause) {
      super(resolutionContext, MessageUtils.buildMessage(resolutionContext, message), cause);
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, String message) {
      super(resolutionContext, MessageUtils.buildMessage(resolutionContext, message, false));
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, Argument argument, String message) {
      super(resolutionContext, MessageUtils.buildMessage(resolutionContext, argument, message, false));
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, FieldInjectionPoint fieldInjectionPoint, Throwable cause) {
      this(resolutionContext, fieldInjectionPoint.getDeclaringBean(), fieldInjectionPoint.getName(), cause);
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, BeanDefinition declaringBean, String fieldName, Throwable cause) {
      super(resolutionContext, MessageUtils.buildMessageForField(resolutionContext, declaringBean, fieldName, null, false), cause);
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, FieldInjectionPoint fieldInjectionPoint, String message) {
      this(resolutionContext, fieldInjectionPoint.getDeclaringBean(), fieldInjectionPoint.getName(), message);
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, BeanDefinition declaringBean, String fieldName, String message) {
      super(resolutionContext, MessageUtils.buildMessageForField(resolutionContext, declaringBean, fieldName, message, false));
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, FieldInjectionPoint fieldInjectionPoint, String message, Throwable cause) {
      this(resolutionContext, fieldInjectionPoint.getDeclaringBean(), fieldInjectionPoint.getName(), message, cause);
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, BeanDefinition declaringBean, String fieldName, String message, Throwable cause) {
      super(resolutionContext, MessageUtils.buildMessageForField(resolutionContext, declaringBean, fieldName, message, false), cause);
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, MethodInjectionPoint methodInjectionPoint, Argument argument, Throwable cause) {
      super(
         resolutionContext,
         MessageUtils.buildMessageForMethod(resolutionContext, methodInjectionPoint.getDeclaringBean(), methodInjectionPoint.getName(), argument, null, false),
         cause
      );
   }

   public DependencyInjectionException(
      BeanResolutionContext resolutionContext, BeanDefinition declaringType, String methodName, Argument argument, Throwable cause
   ) {
      super(resolutionContext, MessageUtils.buildMessageForMethod(resolutionContext, declaringType, methodName, argument, null, false), cause);
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, MethodInjectionPoint methodInjectionPoint, Argument argument, String message) {
      this(resolutionContext, methodInjectionPoint.getDeclaringBean(), methodInjectionPoint.getName(), argument, message);
   }

   public DependencyInjectionException(
      BeanResolutionContext resolutionContext, BeanDefinition declaringType, String methodName, Argument argument, String message
   ) {
      super(resolutionContext, MessageUtils.buildMessageForMethod(resolutionContext, declaringType, methodName, argument, message, false));
   }

   public DependencyInjectionException(BeanResolutionContext resolutionContext, ArgumentConversionContext argumentConversionContext, String property) {
      super(
         resolutionContext,
         MessageUtils.buildMessage(
            resolutionContext, argumentConversionContext.getArgument(), buildConversionMessage(property, argumentConversionContext), false
         )
      );
   }

   public DependencyInjectionException(
      BeanResolutionContext resolutionContext, MethodInjectionPoint methodInjectionPoint, ArgumentConversionContext conversionContext, String property
   ) {
      this(resolutionContext, methodInjectionPoint.getDeclaringBean(), methodInjectionPoint.getName(), conversionContext, property);
   }

   public DependencyInjectionException(
      BeanResolutionContext resolutionContext, BeanDefinition declaringBean, String methodName, ArgumentConversionContext conversionContext, String property
   ) {
      super(
         resolutionContext,
         MessageUtils.buildMessageForMethod(
            resolutionContext, declaringBean, methodName, conversionContext.getArgument(), buildConversionMessage(property, conversionContext), false
         )
      );
   }

   protected DependencyInjectionException(
      BeanResolutionContext resolutionContext, MethodInjectionPoint methodInjectionPoint, Argument argument, String message, boolean circular
   ) {
      this(resolutionContext, methodInjectionPoint.getDeclaringBean(), methodInjectionPoint.getName(), argument, message, circular);
   }

   protected DependencyInjectionException(
      BeanResolutionContext resolutionContext, BeanDefinition declaringType, String methodName, Argument argument, String message, boolean circular
   ) {
      super(resolutionContext, MessageUtils.buildMessageForMethod(resolutionContext, declaringType, methodName, argument, message, circular));
   }

   protected DependencyInjectionException(BeanResolutionContext resolutionContext, FieldInjectionPoint fieldInjectionPoint, String message, boolean circular) {
      this(resolutionContext, fieldInjectionPoint.getDeclaringBean(), fieldInjectionPoint.getName(), message, circular);
   }

   protected DependencyInjectionException(
      BeanResolutionContext resolutionContext, BeanDefinition declaringType, String fieldName, String message, boolean circular
   ) {
      super(resolutionContext, MessageUtils.buildMessageForField(resolutionContext, declaringType, fieldName, message, circular));
   }

   protected DependencyInjectionException(BeanResolutionContext resolutionContext, Argument argument, String message, boolean circular) {
      super(resolutionContext, MessageUtils.buildMessage(resolutionContext, argument, message, circular));
   }

   public static DependencyInjectionException missingProperty(
      BeanResolutionContext resolutionContext, ArgumentConversionContext conversionContext, String property
   ) {
      return new DependencyInjectionException(
         resolutionContext, MessageUtils.buildMessage(resolutionContext, buildConversionMessage(property, conversionContext), false)
      );
   }

   private static String buildConversionMessage(String property, ArgumentConversionContext conversionContext) {
      Optional<ConversionError> lastError = conversionContext.getLastError();
      if (lastError.isPresent()) {
         ConversionError conversionError = (ConversionError)lastError.get();
         return "Error resolving property value ["
            + property
            + "]. Unable to convert value "
            + (String)conversionError.getOriginalValue().map(o -> "[" + o + "]").orElse("")
            + " to target type ["
            + conversionContext.getArgument().getTypeString(true)
            + "] due to: "
            + conversionError.getCause().getMessage();
      } else {
         return "Error resolving property value [" + property + "]. Property doesn't exist";
      }
   }
}
