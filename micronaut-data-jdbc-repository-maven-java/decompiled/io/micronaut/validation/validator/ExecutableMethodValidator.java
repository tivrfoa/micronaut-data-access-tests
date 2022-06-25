package io.micronaut.validation.validator;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.inject.ExecutableMethod;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.executable.ExecutableValidator;

public interface ExecutableMethodValidator extends ExecutableValidator {
   @NonNull
   <T> T createValid(@NonNull Class<T> type, Object... arguments) throws ConstraintViolationException;

   @NonNull
   <T> Set<ConstraintViolation<T>> validateParameters(
      @NonNull T object, @NonNull ExecutableMethod method, @NonNull Object[] parameterValues, @Nullable Class<?>... groups
   );

   @NonNull
   <T> Set<ConstraintViolation<T>> validateParameters(
      @NonNull T object, @NonNull ExecutableMethod method, @NonNull Collection<MutableArgumentValue<?>> argumentValues, @Nullable Class<?>... groups
   );

   @NonNull
   <T> Set<ConstraintViolation<T>> validateReturnValue(
      @NonNull T object, @NonNull ExecutableMethod<?, Object> executableMethod, @Nullable Object returnValue, @Nullable Class<?>... groups
   );

   @NonNull
   <T> Set<ConstraintViolation<T>> validateConstructorParameters(
      @NonNull BeanIntrospection<? extends T> introspection, @NonNull Object[] parameterValues, @Nullable Class<?>... groups
   );

   <T> Set<ConstraintViolation<T>> validateConstructorParameters(
      @NonNull Class<? extends T> beanType, @NonNull Argument<?>[] constructorArguments, @NonNull Object[] parameterValues, @Nullable Class<?>[] groups
   );

   @NonNull
   @Override
   <T> Set<ConstraintViolation<T>> validateParameters(
      @NonNull T object, @NonNull Method method, @NonNull Object[] parameterValues, @Nullable Class<?>... groups
   );

   @NonNull
   @Override
   <T> Set<ConstraintViolation<T>> validateReturnValue(@NonNull T object, @NonNull Method method, @Nullable Object returnValue, @Nullable Class<?>... groups);

   @NonNull
   @Override
   <T> Set<ConstraintViolation<T>> validateConstructorParameters(
      @NonNull Constructor<? extends T> constructor, @NonNull Object[] parameterValues, @Nullable Class<?>... groups
   );

   @NonNull
   @Override
   <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(
      @NonNull Constructor<? extends T> constructor, @NonNull T createdObject, @Nullable Class<?>... groups
   );
}
