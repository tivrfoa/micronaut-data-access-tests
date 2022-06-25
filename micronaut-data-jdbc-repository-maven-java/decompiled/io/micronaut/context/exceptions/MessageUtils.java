package io.micronaut.context.exceptions;

import io.micronaut.context.AbstractBeanResolutionContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;

class MessageUtils {
   static String buildMessage(BeanResolutionContext resolutionContext, String message) {
      BeanResolutionContext.Path path = resolutionContext.getPath();
      boolean hasPath = !path.isEmpty();
      BeanDefinition declaringType;
      if (hasPath) {
         BeanResolutionContext.Segment segment = (BeanResolutionContext.Segment)path.peek();
         declaringType = segment.getDeclaringType();
      } else {
         declaringType = resolutionContext.getRootDefinition();
      }

      String ls = CachedEnvironment.getProperty("line.separator");
      StringBuilder builder = new StringBuilder("Error instantiating bean of type  [");
      builder.append(declaringType.getName()).append("]").append(ls).append(ls);
      if (message != null) {
         builder.append("Message: ").append(message).append(ls);
      }

      if (hasPath) {
         String pathString = path.toString();
         builder.append("Path Taken: ").append(pathString);
      }

      return builder.toString();
   }

   static String buildMessage(BeanResolutionContext resolutionContext, String message, boolean circular) {
      BeanResolutionContext.Segment<?> currentSegment = (BeanResolutionContext.Segment)resolutionContext.getPath().peek();
      if (currentSegment instanceof AbstractBeanResolutionContext.ConstructorSegment) {
         return buildMessage(resolutionContext, currentSegment.getArgument(), message, circular);
      } else if (currentSegment instanceof AbstractBeanResolutionContext.MethodSegment) {
         return buildMessageForMethod(
            resolutionContext, currentSegment.getDeclaringType(), currentSegment.getName(), currentSegment.getArgument(), message, circular
         );
      } else if (currentSegment instanceof AbstractBeanResolutionContext.FieldSegment) {
         return buildMessageForField(resolutionContext, currentSegment.getDeclaringType(), currentSegment.getName(), message, circular);
      } else if (currentSegment instanceof AbstractBeanResolutionContext.AnnotationSegment) {
         return buildMessage(resolutionContext, currentSegment.getArgument(), message, circular);
      } else {
         throw new IllegalStateException("Unknown segment: " + currentSegment);
      }
   }

   static String buildMessageForMethod(
      BeanResolutionContext resolutionContext, BeanDefinition declaringType, String methodName, Argument argument, String message, boolean circular
   ) {
      StringBuilder builder = new StringBuilder("Failed to inject value for parameter [");
      String ls = CachedEnvironment.getProperty("line.separator");
      builder.append(argument.getName())
         .append("] of method [")
         .append(methodName)
         .append("] of class: ")
         .append(declaringType.getName())
         .append(ls)
         .append(ls);
      if (message != null) {
         builder.append("Message: ").append(message).append(ls);
      }

      appendPath(resolutionContext, circular, builder, ls);
      return builder.toString();
   }

   static String buildMessageForField(BeanResolutionContext resolutionContext, BeanDefinition declaringType, String fieldName, String message, boolean circular) {
      StringBuilder builder = new StringBuilder("Failed to inject value for field [");
      String ls = CachedEnvironment.getProperty("line.separator");
      builder.append(fieldName).append("] of class: ").append(declaringType.getName()).append(ls).append(ls);
      if (message != null) {
         builder.append("Message: ").append(message).append(ls);
      }

      appendPath(resolutionContext, circular, builder, ls);
      return builder.toString();
   }

   static String buildMessage(BeanResolutionContext resolutionContext, Argument argument, String message, boolean circular) {
      StringBuilder builder = new StringBuilder("Failed to inject value for parameter [");
      String ls = CachedEnvironment.getProperty("line.separator");
      BeanResolutionContext.Path path = resolutionContext.getPath();
      builder.append(argument.getName())
         .append("] of class: ")
         .append(((BeanResolutionContext.Segment)path.peek()).getDeclaringType().getName())
         .append(ls)
         .append(ls);
      if (message != null) {
         builder.append("Message: ").append(message).append(ls);
      }

      appendPath(circular, builder, ls, path);
      return builder.toString();
   }

   private static void appendPath(BeanResolutionContext resolutionContext, boolean circular, StringBuilder builder, String ls) {
      BeanResolutionContext.Path path = resolutionContext.getPath();
      if (!path.isEmpty()) {
         appendPath(circular, builder, ls, path);
      }

   }

   private static void appendPath(boolean circular, StringBuilder builder, String ls, BeanResolutionContext.Path path) {
      String pathString = circular ? path.toCircularString() : path.toString();
      builder.append("Path Taken: ");
      if (circular) {
         builder.append(ls);
      }

      builder.append(pathString);
   }
}
