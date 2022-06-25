package io.micronaut.validation.validator;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.annotation.AnnotatedElementValidator;
import io.micronaut.inject.qualifiers.TypeArgumentQualifier;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.DefaultConstraintValidators;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

@Internal
public class DefaultAnnotatedElementValidator extends DefaultValidator implements AnnotatedElementValidator {
   public DefaultAnnotatedElementValidator() {
      super(new DefaultValidatorConfiguration().setConstraintValidatorRegistry(new DefaultAnnotatedElementValidator.LocalConstraintValidators()));
   }

   private static final class LocalConstraintValidators extends DefaultConstraintValidators {
      private Map<DefaultConstraintValidators.ValidatorKey, ConstraintValidator> validatorMap;

      private LocalConstraintValidators() {
      }

      @Override
      protected <A extends Annotation, T> Optional<ConstraintValidator> findLocalConstraintValidator(
         @NonNull Class<A> constraintType, @NonNull Class<T> targetType
      ) {
         return this.findConstraintValidatorFromServiceLoader(constraintType, targetType);
      }

      private <A extends Annotation, T> Optional<ConstraintValidator> findConstraintValidatorFromServiceLoader(Class<A> constraintType, Class<T> targetType) {
         if (this.validatorMap == null) {
            this.validatorMap = this.initializeValidatorMap();
         }

         return this.validatorMap.entrySet().stream().filter(entry -> {
            DefaultConstraintValidators.ValidatorKey key = (DefaultConstraintValidators.ValidatorKey)entry.getKey();
            Class[] left = new Class[]{constraintType, targetType};
            return TypeArgumentQualifier.areTypesCompatible(left, Arrays.asList(key.getConstraintType(), key.getTargetType()));
         }).findFirst().map(Entry::getValue);
      }

      private Map<DefaultConstraintValidators.ValidatorKey, ConstraintValidator> initializeValidatorMap() {
         this.validatorMap = new HashMap();

         for(ServiceDefinition<ConstraintValidator> constraintValidator : SoftServiceLoader.load(ConstraintValidator.class)) {
            if (constraintValidator.isPresent()) {
               try {
                  ConstraintValidator validator = constraintValidator.load();
                  Class[] typeArgs = GenericTypeUtils.resolveInterfaceTypeArguments(validator.getClass(), ConstraintValidator.class);
                  if (ArrayUtils.isNotEmpty(typeArgs) && typeArgs.length == 2) {
                     this.validatorMap.put(new DefaultConstraintValidators.ValidatorKey(typeArgs[0], typeArgs[1]), validator);
                  }
               } catch (Exception var6) {
                  System.err.println("WARNING: Could not validator [" + constraintValidator.getName() + "]: " + var6.getMessage());
               }
            }
         }

         return this.validatorMap;
      }
   }
}
