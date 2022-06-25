package io.micronaut.inject;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ArgumentCoercible;

public interface ArgumentInjectionPoint<B, T> extends InjectionPoint<B>, ArgumentCoercible<T> {
   @NonNull
   CallableInjectionPoint<B> getOuterInjectionPoint();

   @NonNull
   Argument<T> getArgument();

   @Override
   default Argument<T> asArgument() {
      return this.getArgument();
   }
}
