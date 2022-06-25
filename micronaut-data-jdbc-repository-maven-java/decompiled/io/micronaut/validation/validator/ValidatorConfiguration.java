package io.micronaut.validation.validator;

import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.MessageSource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.validation.validator.constraints.ConstraintValidatorRegistry;
import io.micronaut.validation.validator.extractors.ValueExtractorRegistry;
import javax.validation.ClockProvider;
import javax.validation.TraversableResolver;

public interface ValidatorConfiguration {
   String PREFIX = "micronaut.validator";
   String ENABLED = "micronaut.validator.enabled";

   @NonNull
   ConstraintValidatorRegistry getConstraintValidatorRegistry();

   @NonNull
   ValueExtractorRegistry getValueExtractorRegistry();

   @NonNull
   ClockProvider getClockProvider();

   @NonNull
   TraversableResolver getTraversableResolver();

   @NonNull
   MessageSource getMessageSource();

   @NonNull
   ExecutionHandleLocator getExecutionHandleLocator();
}
