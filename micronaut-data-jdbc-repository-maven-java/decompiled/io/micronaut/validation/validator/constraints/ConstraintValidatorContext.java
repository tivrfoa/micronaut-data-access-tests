package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import javax.validation.ClockProvider;

public interface ConstraintValidatorContext {
   @NonNull
   ClockProvider getClockProvider();

   @Nullable
   Object getRootBean();

   default void messageTemplate(@Nullable final String messageTemplate) {
      throw new UnsupportedOperationException("not implemented");
   }
}
