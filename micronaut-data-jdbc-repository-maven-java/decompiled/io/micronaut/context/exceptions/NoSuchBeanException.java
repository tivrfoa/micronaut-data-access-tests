package io.micronaut.context.exceptions;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;

public class NoSuchBeanException extends BeanContextException {
   public NoSuchBeanException(@NonNull Class<?> beanType) {
      super("No bean of type [" + beanType.getName() + "] exists." + additionalMessage());
   }

   public NoSuchBeanException(@NonNull Argument<?> beanType) {
      super("No bean of type [" + beanType.getTypeName() + "] exists." + additionalMessage());
   }

   public <T> NoSuchBeanException(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      super(
         "No bean of type ["
            + beanType.getName()
            + "] exists"
            + (qualifier != null ? " for the given qualifier: " + qualifier : "")
            + "."
            + additionalMessage()
      );
   }

   public <T> NoSuchBeanException(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      super(
         "No bean of type ["
            + beanType.getTypeName()
            + "] exists"
            + (qualifier != null ? " for the given qualifier: " + qualifier : "")
            + "."
            + additionalMessage()
      );
   }

   protected NoSuchBeanException(String message) {
      super(message);
   }

   @NonNull
   private static String additionalMessage() {
      return " Make sure the bean is not disabled by bean requirements (enable trace logging for 'io.micronaut.context.condition' to check) and if the bean is enabled then ensure the class is declared a bean and annotation processing is enabled (for Java and Kotlin the 'micronaut-inject-java' dependency should be configured as an annotation processor).";
   }
}
