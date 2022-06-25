package io.micronaut.aop.exceptions;

import io.micronaut.inject.ExecutableMethod;

public class UnimplementedAdviceException extends UnsupportedOperationException {
   public UnimplementedAdviceException(ExecutableMethod<?, ?> method) {
      super("All possible Introduction advise exhausted and no implementation found for method: " + method);
   }
}
