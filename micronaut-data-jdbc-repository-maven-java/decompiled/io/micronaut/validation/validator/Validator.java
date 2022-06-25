package io.micronaut.validation.validator;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;

public interface Validator extends javax.validation.Validator {
   String ANN_VALID = Valid.class.getName();
   String ANN_CONSTRAINT = Constraint.class.getName();

   @NonNull
   ExecutableMethodValidator forExecutables();

   @NonNull
   @Override
   <T> Set<ConstraintViolation<T>> validate(@NonNull T object, Class<?>... groups);

   @NonNull
   <T> Set<ConstraintViolation<T>> validate(@NonNull BeanIntrospection<T> introspection, @NonNull T object, @Nullable Class<?>... groups);

   @NonNull
   @Override
   <T> Set<ConstraintViolation<T>> validateProperty(@NonNull T object, @NonNull String propertyName, Class<?>... groups);

   @NonNull
   @Override
   <T> Set<ConstraintViolation<T>> validateValue(@NonNull Class<T> beanType, @NonNull String propertyName, @Nullable Object value, Class<?>... groups);

   @NonNull
   static Validator getInstance() {
      return new DefaultValidator(new DefaultValidatorConfiguration());
   }
}
