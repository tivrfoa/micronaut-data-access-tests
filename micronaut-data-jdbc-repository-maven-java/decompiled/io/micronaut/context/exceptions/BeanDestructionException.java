package io.micronaut.context.exceptions;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.BeanType;

public class BeanDestructionException extends BeanContextException {
   public BeanDestructionException(@NonNull BeanType<?> beanType, @NonNull Throwable cause) {
      super("Error destroying bean of type [" + beanType.getBeanType() + "]: " + cause.getMessage(), cause);
   }
}
