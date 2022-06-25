package io.micronaut.context.exceptions;

import io.micronaut.context.BeanResolutionContext;
import io.micronaut.inject.BeanType;

public class BeanInstantiationException extends BeanCreationException {
   public BeanInstantiationException(String message, Throwable cause) {
      super(message, cause);
   }

   public BeanInstantiationException(String message) {
      super(message);
   }

   public BeanInstantiationException(BeanResolutionContext resolutionContext, Throwable cause) {
      super(resolutionContext, MessageUtils.buildMessage(resolutionContext, cause.getMessage()), cause);
   }

   public BeanInstantiationException(BeanResolutionContext resolutionContext, String message) {
      super(resolutionContext, MessageUtils.buildMessage(resolutionContext, message));
   }

   public <T> BeanInstantiationException(BeanType<T> beanDefinition, Throwable cause) {
      super(beanDefinition, "Error instantiating bean of type [" + beanDefinition.getName() + "]: " + cause.getMessage(), cause);
   }

   public <T> BeanInstantiationException(BeanType<T> beanDefinition, String message) {
      super(beanDefinition, "Error instantiating bean of type [" + beanDefinition.getName() + "]: " + message);
   }
}
