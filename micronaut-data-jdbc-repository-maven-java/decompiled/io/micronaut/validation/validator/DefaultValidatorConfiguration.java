package io.micronaut.validation.validator;

import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.MessageSource;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.Toggleable;
import io.micronaut.validation.validator.constraints.ConstraintValidatorRegistry;
import io.micronaut.validation.validator.constraints.DefaultConstraintValidators;
import io.micronaut.validation.validator.extractors.DefaultValueExtractors;
import io.micronaut.validation.validator.extractors.ValueExtractorRegistry;
import io.micronaut.validation.validator.messages.DefaultValidationMessages;
import jakarta.inject.Inject;
import java.lang.annotation.ElementType;
import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.ValidatorContext;
import javax.validation.valueextraction.ValueExtractor;

@ConfigurationProperties("micronaut.validator")
public class DefaultValidatorConfiguration implements ValidatorConfiguration, Toggleable, ValidatorContext {
   @Nullable
   private ConstraintValidatorRegistry constraintValidatorRegistry;
   @Nullable
   private ValueExtractorRegistry valueExtractorRegistry;
   @Nullable
   private ClockProvider clockProvider;
   @Nullable
   private TraversableResolver traversableResolver;
   @Nullable
   private MessageSource messageSource;
   @Nullable
   private ExecutionHandleLocator executionHandleLocator;
   private boolean enabled = true;

   @NonNull
   @Override
   public ConstraintValidatorRegistry getConstraintValidatorRegistry() {
      return (ConstraintValidatorRegistry)(this.constraintValidatorRegistry != null ? this.constraintValidatorRegistry : new DefaultConstraintValidators());
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public DefaultValidatorConfiguration setEnabled(boolean enabled) {
      this.enabled = enabled;
      return this;
   }

   @Inject
   public DefaultValidatorConfiguration setConstraintValidatorRegistry(@Nullable ConstraintValidatorRegistry constraintValidatorRegistry) {
      this.constraintValidatorRegistry = constraintValidatorRegistry;
      return this;
   }

   @NonNull
   @Override
   public ValueExtractorRegistry getValueExtractorRegistry() {
      return (ValueExtractorRegistry)(this.valueExtractorRegistry != null ? this.valueExtractorRegistry : new DefaultValueExtractors());
   }

   @Inject
   public DefaultValidatorConfiguration setValueExtractorRegistry(@Nullable ValueExtractorRegistry valueExtractorRegistry) {
      this.valueExtractorRegistry = valueExtractorRegistry;
      return this;
   }

   @NonNull
   @Override
   public ClockProvider getClockProvider() {
      return (ClockProvider)(this.clockProvider != null ? this.clockProvider : new DefaultClockProvider());
   }

   @Inject
   public DefaultValidatorConfiguration setClockProvider(@Nullable ClockProvider clockProvider) {
      this.clockProvider = clockProvider;
      return this;
   }

   @NonNull
   @Override
   public TraversableResolver getTraversableResolver() {
      return this.traversableResolver != null ? this.traversableResolver : new TraversableResolver() {
         @Override
         public boolean isReachable(Object object, Path.Node node, Class<?> rootType, Path path, ElementType elementType) {
            return true;
         }

         @Override
         public boolean isCascadable(Object object, Path.Node node, Class<?> rootType, Path path, ElementType elementType) {
            return true;
         }
      };
   }

   @Inject
   public DefaultValidatorConfiguration setTraversableResolver(@Nullable TraversableResolver traversableResolver) {
      this.traversableResolver = traversableResolver;
      return this;
   }

   @NonNull
   @Override
   public MessageSource getMessageSource() {
      return (MessageSource)(this.messageSource != null ? this.messageSource : new DefaultValidationMessages());
   }

   @Inject
   public DefaultValidatorConfiguration setMessageSource(@Nullable MessageSource messageSource) {
      this.messageSource = messageSource;
      return this;
   }

   @NonNull
   @Override
   public ExecutionHandleLocator getExecutionHandleLocator() {
      return this.executionHandleLocator != null ? this.executionHandleLocator : ExecutionHandleLocator.EMPTY;
   }

   @Inject
   public DefaultValidatorConfiguration setExecutionHandleLocator(@Nullable ExecutionHandleLocator executionHandleLocator) {
      this.executionHandleLocator = executionHandleLocator;
      return this;
   }

   @Override
   public ValidatorContext messageInterpolator(MessageInterpolator messageInterpolator) {
      throw new UnsupportedOperationException("Method messageInterpolator(..) not supported");
   }

   @Override
   public ValidatorContext traversableResolver(TraversableResolver traversableResolver) {
      this.traversableResolver = traversableResolver;
      return this;
   }

   @Override
   public ValidatorContext constraintValidatorFactory(ConstraintValidatorFactory factory) {
      throw new UnsupportedOperationException("Method constraintValidatorFactory(..) not supported");
   }

   @Override
   public ValidatorContext parameterNameProvider(ParameterNameProvider parameterNameProvider) {
      throw new UnsupportedOperationException("Method parameterNameProvider(..) not supported");
   }

   @Override
   public ValidatorContext clockProvider(ClockProvider clockProvider) {
      this.clockProvider = clockProvider;
      return this;
   }

   @Override
   public ValidatorContext addValueExtractor(ValueExtractor<?> extractor) {
      throw new UnsupportedOperationException("Method addValueExtractor(..) not supported");
   }

   @Override
   public javax.validation.Validator getValidator() {
      return new DefaultValidator(this);
   }
}
