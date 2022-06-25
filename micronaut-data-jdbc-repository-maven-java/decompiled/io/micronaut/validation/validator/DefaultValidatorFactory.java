package io.micronaut.validation.validator;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;
import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

@Requires(
   missingBeans = {ValidatorFactory.class}
)
@Internal
@Singleton
public class DefaultValidatorFactory implements ValidatorFactory {
   private final Validator validator;
   private final ValidatorConfiguration configuration;

   protected DefaultValidatorFactory(Validator validator, ValidatorConfiguration configuration) {
      this.validator = validator;
      this.configuration = configuration;
   }

   @Override
   public javax.validation.Validator getValidator() {
      return this.validator;
   }

   @Override
   public ValidatorContext usingContext() {
      return new DefaultValidatorConfiguration();
   }

   @Override
   public MessageInterpolator getMessageInterpolator() {
      throw new UnsupportedOperationException("Method getMessageInterpolator() not supported");
   }

   @Override
   public TraversableResolver getTraversableResolver() {
      return this.configuration.getTraversableResolver();
   }

   @Override
   public ConstraintValidatorFactory getConstraintValidatorFactory() {
      throw new UnsupportedOperationException("Method getConstraintValidatorFactory() not supported");
   }

   @Override
   public ParameterNameProvider getParameterNameProvider() {
      throw new UnsupportedOperationException("Method getParameterNameProvider() not supported");
   }

   @Override
   public ClockProvider getClockProvider() {
      return this.configuration.getClockProvider();
   }

   @Override
   public <T> T unwrap(Class<T> type) {
      throw new UnsupportedOperationException("Method unwrap(..) not supported");
   }

   @Override
   public void close() {
   }
}
